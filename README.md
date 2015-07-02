# refract
**refract** is a 2D, stack-based, esoteric language.

#Differences
Movement can go diagonal

Random movement

```
x1a;
23
a a
;  ;
```

##z
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

##y

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
