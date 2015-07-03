# refract
**refract** is a 2D, stack-based, esoteric language.

##differences from ><>
Most of the language is from [><>](https://esolangs.org/wiki/Fish), but there are some differences.

* `@` now puts the third item on the stack to the top (like GolfScript and every other language).
* `m` pushes 0 if the stack is empty, 1 if not.
* `j` pushes a number input fomr stdin

Code Blocks can be made with `{}`, and are assigned to a character.

```
{21+n}h h
```

Prints 3

You can assign it to *any* character

```
{21+n}  
```

Prints 3 because it assigns it to a space, then calls it with the second space.



Movement can go diagonal, though it doesn't if you don't include `x`, `y`, or `z`.

Random movement can go diagonal (image in example starts in the center).

```
;  ;  ;
 a a a
  678
;a5x1a;
  432
 a a a
;  ;  ;
```

This shows some wrapping and more randomness.

```
x1a;a4
23
a a
;  ;
a   
5
```

`z` and `y` reflect the direction of movement base on where it hits the letter.

```
2
 2
  2
111z
```

**111222**


```
z222
 1
  1
   1
```

**111222**

```
z111
 2
  2
   2
```

**111222**

```
1
z
```

**11**

```
z
1
```

**11**

```
111y
  2
 2
2
```

**111222**

```
y111
 2
  2
   2
```

**111222**
