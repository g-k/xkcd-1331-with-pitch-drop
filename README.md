ClojureScript version of [xkcd 1331: Frequency](http://xkcd.com/1331/) with the [Pitch Drop Experiment](http://smp.uq.edu.au/content/pitch-drop-experiment)

#### Screenshot



#### Notes

Project to learn a bit about core.async.

The pitch drop blinking is actually way too fast.  This problem is
apparently due to a bug in the CLJS core.async timeout implementation
where timeouts larger than the internal skiplist size execute
immediately instead of at the longest possible internal (but I haven't
dug into it too deeply yet). This is kind of great.

The page leaks memory on Chrome 35. This is kind of terrible.
