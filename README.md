# ZenMod
My fork of [ChipmunkMod (7cc5c4f330d47060's fork)](https://code.chipmunk.land/7cc5c4f330d47060/chipmunkmod)

If you are not sure if this MOD is safe to Run or Compile, you can read through every line of code, and compile it with the below instructions. You can also see the commit history by clicking on the `<numbers> commits` button, to make sure nobody has added any `exploits or introduced vulnerabilities` to the code.

If you find any exploits, security issues, etc. in the code, please send me an issue or pull request and I will try to respond to it as soon as possible.

## How to install?
You can use the pre-compiled releases in the [Releases sidebar](https://github.com/vortres/ZenMod/releases), or you can compile it yourself.

To comple it yourself make sure you have a Java 21 JDK installed (this may not work for newer JDK versions), then run `./gradlew build --parallel` for Unix(-like) OSes or `gradlew.bat build` for Windows. If the build was successful, the compiled JAR file should be in "build/libs" directory.

Make sure you have the Fabric loader and Fabric API installed for version 1.21.1, and copy the JAR file to your `mods` folder.

If thats not clear enough, ask your search engine `how to install a fabric mod`.

If it pops up with errors you dont understand [create an issue](https://github.com/vortres/ZenMod/issues/new) about it