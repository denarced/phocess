// Package shared contains shared code for all lib modules.
package shared

import (
	"io"
	"log/slog"
	"os"
	"path/filepath"
	"strings"
	"sync"
	"testing"
)

var (
	// Logger is the logger for the app.
	Logger *slog.Logger
	once   sync.Once
)

func deriveLoggingLevel() slog.Level {
	defaultLevel := slog.LevelInfo
	rawValue, exists := os.LookupEnv("ph_logging_level")
	if !exists {
		return defaultLevel
	}

	value, found := map[string]slog.Level{
		"info":  slog.LevelInfo,
		"debug": slog.LevelDebug,
		"error": slog.LevelError,
	}[strings.ToLower(rawValue)]
	if !found {
		return defaultLevel
	}
	return value
}

// InitLogging initializes logging.
func InitLogging() {
	once.Do(func() {
		var home string
		var err error

		home, err = os.UserHomeDir()
		if err != nil {
			panic(err)
		}

		file, err := os.OpenFile(
			filepath.Join(home, "ph.log"),
			os.O_APPEND|os.O_CREATE|os.O_WRONLY,
			0640)
		if err != nil {
			panic(err)
		}
		initLogger(file, deriveLoggingLevel())
	})
}

// InitTestLogging creates an slog logger that writes to t.Log.
func InitTestLogging(tb testing.TB) {
	once.Do(func() {
		initLogger(&testWriter{tb: tb}, slog.LevelDebug)
	})
}

func initLogger(writer io.Writer, level slog.Level) {
	options := &slog.HandlerOptions{Level: level}
	Logger = slog.New(slog.NewTextHandler(writer, options))
}

type testWriter struct {
	tb testing.TB
}

func (w testWriter) Write(p []byte) (n int, err error) {
	w.tb.Log(string(p))
	return len(p), nil
}
