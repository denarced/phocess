package ph

import (
	"errors"
	"testing"
	"time"

	"github.com/denarced/phocess/shared"
	"github.com/stretchr/testify/assert"
)

func TestIsJpg(t *testing.T) {
	ass := assert.New(t)
	for _, tt := range []struct {
		ext      string
		expected bool
	}{
		{".jpg", true},
		{".JPG", true},
		{"jpg", false},
		{".png", false},
		{".d2", false},
	} {
		ass.Equal(tt.expected, IsJpg(tt.ext), tt.ext)
	}
}

func TestCommImpl(t *testing.T) {
	t.Run("UpdateCount", func(t *testing.T) {
		tests := []struct {
			name        string
			postFunc    func(url, contentType string, body []byte) (*simpleHTTPResponse, error)
			expectError bool
		}{
			{
				name: "Success",
				postFunc: func(_, _ string, _ []byte) (*simpleHTTPResponse, error) {
					return &simpleHTTPResponse{statusCode: 200}, nil
				},
				expectError: false,
			},
			{
				name: "Fail",
				postFunc: func(_, _ string, _ []byte) (*simpleHTTPResponse, error) {
					return &simpleHTTPResponse{statusCode: 500}, nil
				},
				expectError: true,
			},
			{
				name: "PostError",
				postFunc: func(_, _ string, _ []byte) (*simpleHTTPResponse, error) {
					return nil, errors.New("post error")
				},
				expectError: true,
			},
		}

		for _, tt := range tests {
			t.Run(tt.name, func(t *testing.T) {
				shared.InitTestLogging(t)
				ass := assert.New(t)
				comm := &commImpl{
					domain: "http://localhost",
					post:   tt.postFunc,
				}
				err := comm.UpdateCount(1, CountTypeTotal, 10)
				if tt.expectError {
					ass.Error(err)
				} else {
					ass.NoError(err)
				}
			})
		}
	})

	t.Run("RegisterGame", func(t *testing.T) {
		tests := []struct {
			name        string
			postFunc    func(url, contentType string, body []byte) (*simpleHTTPResponse, error)
			expectError bool
			expectID    int64
		}{
			{
				name: "Success",
				postFunc: func(_, _ string, _ []byte) (*simpleHTTPResponse, error) {
					return &simpleHTTPResponse{statusCode: 200, body: []byte(`{"id": 123}`)}, nil
				},
				expectError: false,
				expectID:    123,
			},
			{
				name: "Fail",
				postFunc: func(_, _ string, _ []byte) (*simpleHTTPResponse, error) {
					return &simpleHTTPResponse{statusCode: 500, body: []byte("error")}, nil
				},
				expectError: true,
			},
			{
				name: "JsonError",
				postFunc: func(_, _ string, _ []byte) (*simpleHTTPResponse, error) {
					return &simpleHTTPResponse{
						statusCode: 200,
						body:       []byte(`{"id": "not-a-number"}`),
					}, nil
				},
				expectError: false,
				// Failing to parse the HTTP response body is not treated as a showstopper error.
				// It's just logged, but the function doesn't return an error.
				expectID: -1,
			},
			{
				name: "PostError",
				postFunc: func(_, _ string, _ []byte) (*simpleHTTPResponse, error) {
					return nil, errors.New("post error")
				},
				expectError: true,
			},
		}

		for _, tt := range tests {
			t.Run(tt.name, func(t *testing.T) {
				shared.InitTestLogging(t)
				ass := assert.New(t)
				comm := &commImpl{
					domain: "http://localhost",
					post:   tt.postFunc,
				}
				id, err := comm.RegisterGame(time.Now(), "test-game")
				if tt.expectError {
					ass.Error(err)
				} else {
					ass.NoError(err)
					ass.Equal(tt.expectID, id)
				}
			})
		}
	})
}
