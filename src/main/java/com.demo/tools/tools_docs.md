# Documentation for Directory: tools

**Path:** src/main/java/com.demo\tools

**Total Java files:** 7

**Output mode:** normal

## Summary

- **Files:** 7
- **Classes:** 7
- **Methods:** 49

## Table of Contents

- [BeanToMapUtils.java](#BeanToMapUtils-java)
- [ByteUtils.java](#ByteUtils-java)
- [HmacCoder.java](#HmacCoder-java)
- [HttpServletUtils.java](#HttpServletUtils-java)
- [JsonUtils.java](#JsonUtils-java)
- [MD5Util.java](#MD5Util-java)
- [SignUtils.java](#SignUtils-java)

## BeanToMapUtils.java

### Classes

#### BeanToMapUtils

### Methods

#### `Map toMap(Object obj)`

#### `List toListMap(List list)`

---

## ByteUtils.java

### Classes

#### ByteUtils

### Properties (Getters/Setters)

**javaint:**
- **getJavaInt()**: int

**mcuint:**
- **getMcuInt()**: int

### Methods

#### `int toUnsignedInt(String data)`

#### `int toUnsignedInt(byte data)`

#### `int toUnsignedInts(byte datas)`

#### `int toUnsignedInts(String datas)`

#### `byte toByte(int data)`

#### `byte toByte(String hex)`

#### `byte toBytes(int datas)`

#### `byte toBytes(String hexs)`

#### `String to16Hex(Byte data)`

#### `String to16Hex(Integer data)`

#### `String to16Hexs(Byte datas)`

#### `String to16Hexs(byte datas)`

#### `String to16Hexs(Integer datas)`

#### `String to16Hexs(int datas)`

---

## HmacCoder.java

### Classes

#### HmacCoder

### Methods

#### `byte encrypt(byte data, String key, String type)`

#### `String encrypt(String data, String key, String type)`

#### `String byteArrayToHexString(byte b)`

#### `void main(String args)`

---

## HttpServletUtils.java

### Classes

#### HttpServletUtils

### Properties (Getters/Setters)

**module:**
- **getModule()**: String

**page:**


**params:**
- **getParams()**: Map

**realcontextpath:**
- **getRealContextpath()**: String

**realscheme:**
- **getRealScheme()**: String

**realurl:**
- **getRealUrl()**: String

### Methods

#### `HttpServletRequest getHttpServletRequest()`

#### `HttpServletResponse getHttpServletResponse()`

---

## JsonUtils.java

### Classes

#### JsonUtils

### Methods

#### `ObjectMapper getMapper()`

#### `String toJson(Object obj)`

#### `T toObject(String json, Class clazz)`

#### `List toList(String json, Class clazz)`

#### `JavaType getCollectionType(Class collectionClass, Class elementClasses)`

---

## MD5Util.java

### Classes

#### MD5Util

### Properties (Getters/Setters)

**filemd5:**
- **getFileMD5()**: String

**stringmd5:**
- **getStringMD5()**: String

**substr:**
- **getSubStr()**: String

### Methods

#### `String createPaddingString(int n, char pad)`

#### `String MD5(ByteBuffer buffer)`

---

## SignUtils.java

### Classes

#### SignUtils

### Properties (Getters/Setters)

**sign:**
- **getSign()**: String

---

