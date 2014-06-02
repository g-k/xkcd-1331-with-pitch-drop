ClojureScript version of [xkcd 1331: Frequency](http://xkcd.com/1331/) with the [Pitch Drop Experiment](http://smp.uq.edu.au/content/pitch-drop-experiment)

#### Screenshot

![xkcd 1331 pitch drop](xkcd-screen.png?raw=true "xkcd 1331 pitch drop")

#### TODO

- [ ] The pitch drop (the only feature) is blinking way too fast. 
   * `window.setTimeout` returns immediately on Chrome 35 and Firefox 29.0.1:

```
> window.setTimeout(function () { console.log("done"); }, Math.pow(2, 31))
2
done
> window.setTimeout(function () { console.log("done"); }, Math.pow(2, 31) - 1)
3
```

   * `setTimeout` is [defined in the HTML5 spec not the ECMAScript spec](http://stackoverflow.com/questions/8852198/settimeout-if-not-defined-in-ecmascript-spec-where-can-i-learn-how-it-works) 

   * MDN notes:

> Browsers including Internet Explorer, Chrome, Safari, and Firefox store the delay as a 32-bit signed Integer internally. This causes an Integer overflow when using delays larger than 2147483647, resulting in the timeout being executed immediately.

https://developer.mozilla.org/en-US/docs/Web/API/window.setTimeout#Minimum.2F_maximum_delay_and_timeout_nesting

  which checks out:
   
```
> (Math.pow(2, 31) - 1).toString(16)
"7fffffff"
> (Math.pow(2, 31)).toString(16)
"80000000"
```
  
  but is kind of weird considering JS Numbers are 64-bit IEEE 754 floating point:

```
> Number.MAX_SAFE_INTEGER.toString(16)
"1fffffffffffff"
```

- [ ] slowish memory leak on Chrome 35 (other browsers untested)
- [ ] overlapping blinking blocks when resized
