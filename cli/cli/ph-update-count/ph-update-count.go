package main

import (
	"fmt"
	"os"
	"path/filepath"

	"github.com/alecthomas/kong"
	"github.com/denarced/phocess/lib/ph"
	"github.com/denarced/phocess/shared"
)

var CLI struct {
	CountType ph.CountType `arg:"" enum:"total,first,final" help:"Count type."`
}

type exitError struct {
	err  error
	code int
}

func main() {
	kong.Parse(&CLI)
	shared.InitLogging()
	shared.Logger.Info("Start.", "CLI arguments", os.Args)

	phID, exitErr := readID()
	if exitErr != nil {
		fmt.Fprintf(os.Stderr, "%s\n", exitErr.err)
		os.Exit(exitErr.code)
	}

	dirs := selectDirs(CLI.CountType)
	count := countPhotos(dirs)
	comm, err := ph.NewCommunicator()
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error creating communicator: %s\n", err)
		os.Exit(20)
	}
	err = comm.UpdateCount(phID, CLI.CountType, count)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error updating count: %s\n", err)
		os.Exit(21)
	}
}

func readID() (int64, *exitError) {
	shared.Logger.Info("Read ID.")
	b, err := os.ReadFile(ph.IDFilen)
	if err != nil {
		if os.IsNotExist(err) {
			shared.Logger.Info("Error reading ID file, it doesn't exist.", "file", ph.IDFilen)
			return 0, &exitError{
				err:  fmt.Errorf("file %s does not exist", ph.IDFilen),
				code: 10,
			}
		}
		shared.Logger.Warn("Error reading ID file.", "file", ph.IDFilen, "error", err)
		return 0, &exitError{
			err:  fmt.Errorf("error reading file %s - %w", ph.IDFilen, err),
			code: 11,
		}
	}
	var phID int64
	count, err := fmt.Sscan(string(b), &phID)
	if count != 1 || err != nil {
		shared.Logger.Warn("Error parsing ID file.", "file", ph.IDFilen, "error", err)
		return 0, &exitError{
			err: fmt.Errorf(
				"file %s should contain a single integer but parsing failed - %w",
				ph.IDFilen,
				err),
			code: 12,
		}
	}

	return phID, nil
}

func selectDirs(ct ph.CountType) []string {
	logger := shared.Logger.With("count type", ct)
	canonDirs := listCanonDirs()
	if ct == ph.CountTypeTotal {
		logger.Debug("Dirs selected.", "dirs", canonDirs)
		return canonDirs
	}
	var length int
	switch ct {
	case ph.CountTypeFirst:
		length = 4
	case ph.CountTypeFinal:
		length = 3
	default:
		panic("unknown count type: " + string(ct))
	}

	var dirs []string
	for _, each := range canonDirs {
		if len(each) == length {
			dirs = append(dirs, each)
		}
	}
	logger.Debug("Dirs selected.", "dirs", dirs)
	return dirs
}

func listCanonDirs() []string {
	entries, err := os.ReadDir(".")
	if err != nil {
		panic(err)
	}
	var dirs []string
	for _, each := range entries {
		if !each.IsDir() {
			continue
		}
		name := each.Name()
		num, rest := separateIntAndString(name)
		if num < 100 || 200 <= num {
			continue
		}
		if rest != "C" && rest != "CANON" && rest != "" {
			continue
		}
		dirs = append(dirs, name)
	}
	return dirs
}

func separateIntAndString(s string) (int, string) {
	var n, i int
	for i = 0; i < len(s); i++ {
		c := s[i]
		if '0' <= c && c <= '9' {
			n = n*10 + int(c-'0')
			continue
		}
		break
	}
	return n, s[i:]
}

func countPhotos(dirs []string) int {
	shared.Logger.Info("Count photos.", "dirs", dirs)
	count := 0
	for _, dir := range dirs {
		entries, err := os.ReadDir(dir)
		if err != nil {
			panic(err)
		}
		for _, each := range entries {
			name := each.Name()
			if ph.IsJpg(filepath.Ext(name)) {
				count++
			}
		}
	}
	return count
}
