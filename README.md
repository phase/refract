# refract
**refract** is a 2D, stack-based, esoteric language.

##differences
Most of the language is from [><>](https://esolangs.org/wiki/Fish), but there are some differences.

Movement can go diagonal, though it doesn't if you don't include `x`, `y`, or `z`.

Random movement can go diagonal.

```
x1a;
23
a a
;  ;
```

###`z`
The stack contents are shown after each example.

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

###`y`

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

```
1
y
2
^
```

**1221**

###`#`

```
1
#
```

**11**

```
 1
#
```

**11**
