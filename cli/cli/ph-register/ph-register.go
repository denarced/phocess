package main

import (
	"fmt"
	"os"
	"time"

	"github.com/alecthomas/kong"
	"github.com/denarced/phocess/lib/ph"
	"github.com/denarced/phocess/shared"
)

var CLI struct {
	Date time.Time `arg:"" help:"Date of the game in 2006-01-02 format." format:"2006-01-02"`
	Name string    `arg:"" help:"Name of the game to register."`
}

func main() {
	shared.InitLogging()
	shared.Logger.Info("Start.", "CLI arguments", os.Args)
	kong.Parse(&CLI)
	comm, err := ph.NewCommunicator()
	if err != nil {
		fmt.Fprintf(os.Stderr, "Failed to create communicator: %s\n", err)
		os.Exit(10)
	}
	id, err := comm.RegisterGame(CLI.Date, CLI.Name)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Failed to register game: %s\n", err)
		os.Exit(11)
	}
	if id < 0 {
		fmt.Fprintln(
			os.Stderr,
			"While the game registration itself was a success, "+
				"the server seems to have returned an unexpected response "+
				"and therefore we don't have an ID for the game.")
		os.Exit(13)
	}
	fmt.Printf("Registered game with ID %d\n", id)
	err = os.WriteFile(ph.IDFilen, fmt.Appendf(nil, "%d", id), 0600)
	if err != nil {
		shared.Logger.Warn("Failed to write ID file.", "file", ph.IDFilen, "error", err)
		fmt.Fprintf(os.Stderr, "Failed to write ID file: %s\n", err)
		os.Exit(12)
	}
}
