# Parser

An error that we regularly ran into was that the CLASSPATH
    was not properly set up for ANTLR.

Please make sure the working directory looks like this:

    inputs (folder)
    outputs (folder)
    Driver.java
    Grading_Script.sh
    Gram.g4
    Micro.sh
    README

Please put all inputs and outputs in their respective folders.
Keep everything else in the same directory.

Micro.sh and Grading_Script.sh should be executable.

Within Grading_Script.sh:
    The location of testcases can remain unchanged.
    Line 12 should read: bash Micro.sh $i > usertest/$output.

Call the Grading_Script.sh with: bash Grading_Script.sh

It does take some time to test because it recompiles
    everything each run through.

Thank you!
