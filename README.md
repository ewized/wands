# Wands

This plugin will create various wands that are fun to mess around with.

## Development

Wands uses gradle to add a task that will let you run `Wands` right from your cloned repo.
To get this setup all you need to do is have forge installed on the client.
Then run `./gradlew :startForgeClient` this will compile `Wands` and start the client.
The client game dir will be in `/run/forge-client/`.

## Building

To build this plugin all you need to run is `./gradlew build` this will compile both `Wands` and `Utilities`.
Once you have both of the _jars_ you can place them in your mods folder.

 - **Wands** `/build/libs/wands-x.x.x-xxxxxxxx.jar`
 - **Utilities** `/utilities/sponge/build/libs/utilities-x.x.x-xxxxxxxx-all.jar`

## Installing for Production

To install this plugin, you must generate two jars from this repo.
The first jar is `wands-x.x.x-xxxxxxxx.jar` and the other is `utilities-x.x.x-xxxxxxxx-all.jar`.
**Wands** is the plugin it's self and **Utilities** is the lib plugin that contains common code.
Wands requires to use the version that is a git module in this repo. The reason is that `Utilities`
is constantly upgrading its codebase.

## License

The code in this project is under the following license.

> The MIT License (MIT)

```
 Copyright (C) 2016 ewized

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
```
