--- Tamriel Data Filepatcher ---

This archive contains the Tamriel Data Filepatcher, an Eclipse project written in Java.

It makes use of JSqlParser (licensed under Apache License v2.0, or GPL 2.0, LGPL 2.0 and later) and Jep Java (licensed under GPL 3.0. 
The filepatcher itself is licensed under the GPL 3.0 (a copy is provided in the root directory of this archive).

--- Function ---
The filepatcher reads a translation.txt, which needs to be in the same directory as the filepatcher itself and replaces IDs in
ESP (Elder Scrolls Plugin) and ESM (Elder Scrolls Master) files for The Elder Scrolls III: Morrowind.

The only IDs it cannot replace are LTEX (local textures) and IDs within compiled scripts (simply recompiling all scripts will solve the later problem). 
It cannot repair references or automatically redirect them.

--- Terms and Conditions ---

The filepatcher sourcecode was never intended to be published. 
As such, it has not been cleaned up, and includes both the JSqlParser and Jep Java libraries that were used to create it.

It is provided as-is, without further support or warranty for life, limb, and file integrity, in the hope that someone else will find it useful and extend on its functionality.
