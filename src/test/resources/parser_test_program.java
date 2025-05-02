/**
 * This file represents an example of Java program with all the allowed statements from the parser.
 */

/*
    This file will be used as final test from parsers and visitors to verify the validity of the program.
 */

// Assignment of all primitive types
byte b = 1;
byte bCast = (byte) b;
short s = 2;
short sCast = (short) s;
char c = 'c';
char cCast = (char) c;
int i = 4;
int iCast = (int) i;
long l = 5L;
long lCast = (long) l;
float f = 6.0f;
float fCast = (float) f;
double d = 7.0d;
double dCast = (double) d;
boolean bo = true;
boolean boCast = (boolean) bo;

final int finalI = 3;

// Re-assignments
i = -4;
l = i;
l = 5;
f = i;
f = l;
f = 6;
d = i;
d = l;
d = f;
d = 7;
d = 7.0f;
d = 7.0;
bo = false;
bo = !bo;

// Assignment of all wrapper types
Byte bW = 1;
Byte bWNull = null;
Byte bWCast = (Byte) bW;
Byte bWCastNull = (Byte) null;
Short sW = 2;
Short sWNull = null;
Short sWCast = (Short) sW;
Short sWCastNull = (Short) null;
Character cW = 'c';
Character cWNull = null;
Character cWCast = (Character) cW;
Character cWCastNull = (Character) null;
Integer iW = 4;
Integer iWNull = null;
Integer iWCast = (Integer) iW;
Integer iWCastNull = (Integer) null;
Long lW = 5L;
Long lWNull = null;
Long lWCast = (Long) lW;
Long lWCastNull = (Long) null;
Float fW = 6.0f;
Float fWNull = null;
Float fWCast = (Float) fW;
Float fWCastNull = (Float) null;
Double dW = 7.0d;
Double dWNull = null;
Double dWCast = (Double) dW;
Double dWCastNull = (Double) null;
Boolean boW = true;
Boolean boWNull = null;
Boolean boWCast = (Boolean) boW;
Boolean boWCastNull = (Boolean) null;
String stW = "Hello world";
String stWCast = (String) stW;
String stWCastNull = (String) null;

// Binary operations
i = i + 1 - 1 * 1 / 1 % 2 & 1 | 1 ^ 1 << 2 >> 1 >>> 1;
i = ((((i + 1) - (1 * 1)) / ((1 % 2) & (1 | 1))) ^ ((1 << 2) >> (1 >>> 1)));
i = (((i + (1 - 1))) * (((1 / 1)) % ((1 & 1))) | (((1 ^ 1) << (2 >> 1)) >>> 1));
i = (1 * 1) + (2 * 2);
i = (1 * 1) - (2 * 2);
i = i - 1 * 1 / 1 % 2 & 1 | 1 ^ 1 << 2 >> 1 >>> 1;
i = i * 1 / 1 % 2 & 1 | 1 ^ 1 << 2 >> 1 >>> 1;
i = i / 1 % 2 & 1 | 1 ^ 1 << 2 >> 1 >>> 1;
i = i % 1 & 1 | 1 ^ 1 << 2 >> 1 >>> 1;
i = i & 1 | 1 ^ 1 << 2 >> 1 >>> 1;
i = i | 1 ^ 1 << 2 >> 1 >>> 1;
i = i ^ 1 << 2 >> 1 >>> 1;
i = i << 2 >> 1 >>> 1;
i = i >> 1 >>> 1;
i = i >>> 1;
l = l + 1 - 1 * 1 / 1 % 2 & 1 | 1 ^ 1 << 2 >> 1 >>> 1;
f = f + 1 - 1 * 1 / 1 % 2;
d = d + 1 - 1 * 1 / 1 % 2;
d = (double) ++d;
d = (double) d++;
d = (double) --d;
d = (double) d--;
d = (double) -2.0;
bo = true == false != true && false || true;
bo = ((true == (false != true)) && (false || true));
bo = (((true == false) != (true && false)) || true);
bo = false != true && false || true;
bo = true && false || true;
bo = false || true;
bo = d == d && bo || false;
bo = f != f && bo || false;
bo = true && bo || false;
bo = bo || false;
bo = i < d || i <= d || f >= d || f > d;
bo = (((i < d) || (i <= d)) || ((f >= d) || (f > d)));

// Re-Assignment operations
i++;
i--;
++i;
--i;
i += 1;
i -= 1;
i *= 1;
i /= 1;
i %= 1;
i &= 1;
i |= 1;
i ^= 2;
i <<= 1;
i >>= 1;
i >>>= 1;

// Method call
System.out.printf("%s %s\n", "Hello", "world");

// New keyword
String string = new String("Hello");
String stringCast = (String) string;
int[] staticArray = new int[1];
int[] staticArrayCast = (int[]) staticArray;
int[] dynamicArray = new int[]{1, 2};
int[] dynamicArrayCast = (int[]) dynamicArray;
String[] stringStaticArray = new String[1];
String[] stringStaticArrayCast = (String[]) stringStaticArray;
String[] stringDynamicArray = new String[]{"1", "2"};
String[] stringDynamicArrayCast = (String[]) stringDynamicArray;

// If statement
int n;

if (true) {
    n = 1;
}

if (true) {
    n = 1;
} else if (false) {
    n = 2;
}

if (true) {
    n = 1;
} else if (false) {
    n = 2;
} else {
    n = 3;
}

// While statement
n = 10;
while (n > 0) {
    n--;
    if (n > 0) break;
    else continue;
}

n = 10;
while (n > 0) n--;

// Do statement
n = 10;
do {
    n--;
    if (n > 0) break;
    else continue;
} while (n > 0);

n = 10;
do n--;
while (n > 0);

// For statement
int j;
for (j = 1; j < 10; j++) {
    if (j == 9) continue;
    else break;
}
for (int k = 0; k < 10; k++)
    if (j == 9) continue;
    else break;

for (; j < 10; j++) break;
for (int k = 0; ; k++) break;
for (int k = 0; k < 10; ) break;
for (int k = 0; ; ) break;
for (; ; j++) break;
for (; j < 10;) break;

// Switch statement
n = 20;

switch (n) {
    case 2: return true;
    case 10: {
        return "Hello";
    }
    case 20: {
        System.out.println("Switch successful!");
    }
    default: {
        System.out.println("Error");
    }
}

// Try statement
try {
    System.out.println("Try statement successful!");
} catch (IllegalArgumentException | IllegalStateException e) {
    System.out.println("An error occurred while executing the try statement...");
} finally {
    System.out.println("Finally block always executed, nice!");
}

try (java.io.ByteArrayInputStream output = new java.io.ByteArrayInputStream("test".getBytes())) {
    System.out.println(output.toString());
} catch (IOException e) {
    throw new RuntimeException(e);
}

// Enhanced for statement
int[] array = new int[]{1, 2, 3, 4, 5};
int[] arrayCast = (int[]) array;

for (int a : array) System.out.println(a);

array[4] = array[4] * 4;
System.out.println("Last element of array is: " + array[4]);

int[][] arrayOfArray = new int[][]{
        new int[]{1, 2, 3},
        new int[]{4, 5, 6},
        new int[]{7, 8, 9},
};

for (int[] a : arrayOfArray) {
    System.out.println(Arrays.toString(a));
    if (a == null) break;
    else continue;
}

// This
System.out.println("The previous value was " + this.publicField);
this.publicField = 10;
System.out.println("Now it has been updated to " + this.publicField);

return bW.toString().toString().toString();
