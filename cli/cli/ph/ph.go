// Package main implements the main CLI.
package main

import (
	"os"

	"github.com/denarced/phocess/shared"
)

func main() {
	shared.InitLogging()
	shared.Logger.Info("Start.", "CLI arguments", os.Args)
	shared.Logger.Info("Done.")
}
