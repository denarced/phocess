package main

import (
	"testing"

	"github.com/stretchr/testify/require"
)

func TestSeparateIntAndString(t *testing.T) {
	run := func(name, input string, expectedInt int, expectedString string) {
		t.Run(name, func(t *testing.T) {
			req := require.New(t)
			num, s := separateIntAndString(input)
			req.Equal(expectedInt, num)
			req.Equal(expectedString, s)
		})
	}
	run("empty", "", 0, "")
	run("no number", "dork", 0, "dork")
	run("only a number", "123", 123, "")
	run("number plus extra", "199abc", 199, "abc")
	run("minimal complete", "9z", 9, "z")
}
