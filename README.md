Hi Professor!

There are a few aspects in which this project differs from the proposal:

- The edit distance function was overhauled to be much faster (orders of magnitude, maybe). However, the old one can still be re-enabled in the debug menu. The proposed edit distance function scaled so bad that it is disabled by default to prevent hanging. Again, it can be re-enabled.
- .multifasta files (instead of just .fasta files) are loaded by default in case you want to play around with that. This doesn't change anything in almost every case, but is enabled for your convenience. You can disable it the debug menu too.
- Lots of other new settings can be enabled through the debug menu. You don't need to play with them all if you don't want to, they're mostly there for fun. Check the code documentation for more information; according to `cloc`, at the time of this writing, there are about 571 lines of comments, and 1133 lines of code, so you can explore it if you want to.
- More optimization was done than is strictly required. In fact, the majority of the time spent on this project was probably spent optimizing. I like optimization.
- There are a few scripts here that might interest you but aren't part of the project.
  - `build.sh` makes a runnable .jar file out of the project.
  - `splitfasta.py` splits a giant UniProt dump from stdin into files.
  - `makegroups.sh` splits a giant UniProt dump from a hardcoded location into sample folders of different sizes
- The project is already built for you as `release.jar`, which might be easier for you to run. Just do `java -jar release.jar`. You can also add `-ea` to run with assertions, which is necessary for properly verifying trees (`java -ea -jar release.jar`). Lastly, you can specify a directory as an argument to load FASTA files from, otherwise it will load them from the current directory.

## Samples

Samples are taken from UniProt. In samples.zip ([here](https://drive.google.com/file/d/1cwoY9zfOlaTAcOpgnmJDP5WCUrEcFADW/view?usp=sharing)), they're organized in the samples folder. **Be careful with the sample files.** Each `0x.zip` file has more files than the previous one, starting from about 10 files to **upwards of 50,000 files.** It is very easy to hang your machine if your hardware or software is not equipped to handle this kind of extraction. Each file ranges from a few hundred bytes to several kilobytes. Each subset of files is mutually exclusive, so there will be nothing in `00.zip` that is in `06.zip`, and vice versa, for example.

There is also `faulty.zip`, which has two faulty FASTA files that can be used to test if error handling works correctly. (It does.)