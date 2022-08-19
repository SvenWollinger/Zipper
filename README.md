# Zipper

Zipper is a utility software and library.

It can be used in your project to make Zipping/Unzipping easier.

# Features:
- Zip / Unzip as a library
- Work as command line utility (run Zipper.jar -help)
- Load .json files (described below) to automatically run zipping/unzipping

# Example .json file

```
{
    "includeConfig": true,
    "includeLog": true,
    "log": true,
    "formatOutput": true,
    "method": "ZIP",
    "input": [
        "D:/common/important/",
        "D:/common/test.txt",
        "%appdata%/.minecraft/"
    ],
    "output": [
        "H:/Backups/Backup_%dd%_%mm%_%yyyy%__%hh%_%mm%_%ss%_%sss%.zip",
        "G:/Backups/latest.zip"
    ]
}
```
Explanation:

Key | Meaning
--- | ---
includeConfig | Include the json Config file named "ZipperConfig.json"
includeLog | Include a log of what was done named "ZipperLog.txt"
log | Log whats happening in the console
formatOutput | Replace %dd%, %mm% etc with data of time and date
method | ZIP or UNZIP
input | List of files/folders to add to the input list
output | List of files/folders to add to the output list

# License

MIT License

Copyright (c) 2022 Sven Wollinger

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.