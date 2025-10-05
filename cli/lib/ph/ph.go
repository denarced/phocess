// Package ph.
package ph

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"path/filepath"
	"time"

	"github.com/denarced/phocess/shared"
	"github.com/pelletier/go-toml/v2"
)

type CountType string

const (
	CountTypeTotal CountType = "total"
	CountTypeFirst CountType = "first"
	CountTypeFinal CountType = "final"

	IDFilen = "ph.id"
)

func IsJpg(ext string) bool {
	if len(ext) != 4 {
		return false
	}
	l := len(ext)
	c := ext[l-1]
	if c != 'g' && c != 'G' {
		return false
	}
	c = ext[l-2]
	if c != 'p' && c != 'P' {
		return false
	}
	c = ext[l-3]
	if c != 'j' && c != 'J' {
		return false
	}
	if ext[l-4] != '.' {
		return false
	}
	return true
}

type Commmunicator interface {
	UpdateCount(phID int64, ct CountType, count int) error
	RegisterGame(date time.Time, name string) (int64, error)
}

type simpleHTTPResponse struct {
	statusCode int
	body       []byte
}

type commImpl struct {
	domain string
	post   func(url, contentType string, body []byte) (*simpleHTTPResponse, error)
}

func (v *commImpl) UpdateCount(phID int64, ct CountType, count int) error {
	url := fmt.Sprintf("%s/v1/games/%d/%s/%d", v.domain, phID, ct, count)
	shared.Logger.Info("Update cloud.", "url", url)
	fmt.Println("Update count.", "ID", phID, "count type", ct, "count", count)
	poster := v.post
	if poster == nil {
		poster = doPost
	}
	res, err := poster(url, "", nil)
	if err != nil {
		shared.Logger.Warn("Error updating count.", "error", err)
		return err
	}
	if 200 <= res.statusCode && res.statusCode < 300 {
		shared.Logger.Info("Count update HTTP request was a success.")
		return nil
	}
	return fmt.Errorf("count update HTTP request failed with status code %d", res.statusCode)
}

func doPost(url, contentType string, body []byte) (*simpleHTTPResponse, error) {
	var reader io.Reader
	if body != nil {
		reader = bytes.NewReader(body)
	}
	res, err := http.Post(url, contentType, reader)
	if err == nil {
		defer res.Body.Close()
		b, err := io.ReadAll(res.Body)
		if err == nil {
			return &simpleHTTPResponse{statusCode: res.StatusCode, body: b}, nil
		}
		return nil, fmt.Errorf("error reading HTTP response body - %w", err)
	}
	return nil, err
}

func (v *commImpl) RegisterGame(date time.Time, name string) (int64, error) {
	type regReq struct {
		Date string `json:"date"`
		Name string `json:"name"`
	}
	url := fmt.Sprintf("%s/v1/games", v.domain)
	req := regReq{Date: date.Format("2006-01-02"), Name: name}
	shared.Logger.Info("Register game.", "url", url, "req", req)
	b, err := json.Marshal(req)
	var gameID int64 = -1
	if err != nil {
		shared.Logger.Error("Error encoding JSON.", "error", err)
		return gameID, fmt.Errorf("error marshaling HTTP request body - %w", err)
	}

	var res *simpleHTTPResponse
	poster := v.post
	if poster == nil {
		poster = doPost
	}
	res, err = poster(url, "application/json", b)
	if err != nil {
		shared.Logger.Warn("Failed to execute HTTP POST request to register a game.", "error", err)
		return gameID, fmt.Errorf("failed to perform HTTP request to register a game - %w", err)
	}

	if 200 <= res.statusCode && res.statusCode < 300 {
		b = res.body
		type regRes struct {
			GameID int64 `json:"id"`
		}
		var resBody regRes
		err = json.Unmarshal(b, &resBody)
		if err == nil {
			gameID = resBody.GameID
		} else {
			shared.Logger.Error(
				"Failed to unmarshal HTTP response body for successful game registration, "+
					"to parse game ID.", "err", err, "body", string(b))
		}

		shared.Logger.Info(
			"Game registered successfully.",
			"status code", res.statusCode,
			"date", date,
			"name", name)
		return gameID, nil
	}

	responseBody := string(b)
	shared.Logger.Error("Game registration failed.", "body", responseBody)
	return gameID, fmt.Errorf(
		"game registration failed with status code %d and body %s",
		res.statusCode,
		responseBody)
}

func NewCommunicator() (Commmunicator, error) {
	conf, err := loadConfig()
	if err != nil {
		return nil, err
	}
	return &commImpl{domain: conf.Backend.URL}, nil
}

type backendConfig struct {
	URL string `toml:"url"`
}

type config struct {
	Backend backendConfig `toml:"backend"`
}

func loadConfig() (*config, error) {
	configFilep, err := deriveConfigFilepath()
	if err != nil {
		return nil, fmt.Errorf("error deriving config filepath - %w", err)
	}
	b, err := os.ReadFile(configFilep)
	if err != nil {
		return nil, fmt.Errorf("error reading config file %s - %w", configFilep, err)
	}
	var conf config
	err = toml.Unmarshal(b, &conf)
	if err != nil {
		return nil, fmt.Errorf("error parsing config file %s - %w", configFilep, err)
	}
	return &conf, nil
}

func deriveConfigFilepath() (string, error) {
	homeDir, err := os.UserHomeDir()
	if err != nil {
		return "", fmt.Errorf("error getting home dir - %w", err)
	}
	return filepath.Join(homeDir, ".config", "ph", "config.toml"), nil
}
