/**
 * It's a class that allows you to easily read and write JSON files
 */
package co.plocki.json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

@SuppressWarnings(
        {
                "unused",
                "WeakerAccess",
                "ResultOfMethodCallIgnored",
                "SameParameterValue",
                "UnusedReturnValue",
                "SpellCheckingInspection",
                "FieldCanBeLocal"
        })
public class JSONFile {

    // Creating a file object.
    private final File file;

    // Declaring a variable called mapperVersion and assigning it the value of 1.0.
    private final String mapperVersion = "1.0";
    // Creating a JSONObject object.
    private JSONObject object;
    // Declaring a variable called isNew and initializing it to true.
    private final boolean isNew;

    public JSONFile(String filePath, JSONValue... objects) {
        object = null;
        file = new File(Paths.get("").toAbsolutePath() + File.separator + filePath);
        if(!file.exists()) {
            if(!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            JSONObject json = new JSONObject();
            json.put("mapperVersion", mapperVersion);
            for(JSONValue object : objects) {
                json.put(object.objectName(), object.object());
            }
            writer.println(new BeautifulJson().beautiful(json.toString()));
            writer.flush();
            writer.close();
            isNew = true;
        } else {
            isNew = false;
        }
        try {
            object = new JSONObject(new String(Files.readAllBytes(file.toPath())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns true if the object is new, false otherwise.
     *
     * @return isNew
     */
    public boolean isNew() {
        return isNew;
    }

    /**
     * This function returns the JSONObject that was created from the JSON file
     *
     * @return A JSONObject
     */
    public JSONObject getFileObject() throws IOException {
        return object;
    }

    /**
     * > This function returns the version of the mapper
     *
     * @return The version of the mapper.
     */
    public String getMapperVersion() {
        return mapperVersion;
    }

    /**
     * This function returns the file.
     *
     * @return The file object.
     */
    public File getFile() {
        return file;
    }

    /**
     * It deletes the file, creates a new file, writes the JSON to the file, and then closes the file
     */
    public void save() throws IOException {
        file.delete();
        file.createNewFile();
        object.remove("mapperVersion");
        object.put("mapperVersion", mapperVersion);
        PrintWriter writer = new PrintWriter(file);
        writer.println(object.toString());
        writer.flush();
        writer.close();
    }

    public void setNull(String key) {
        object.put(key, JSONObject.NULL);
    }

    /**
     * It saves the object to a file
     *
     * @param filePath The path to the file you want to save to.
     */
    public void save(String filePath) throws IOException {
        File newFile = new File(filePath);
        if(!newFile.exists()) {
            newFile.createNewFile();
        } else {
            newFile.delete();
            newFile.createNewFile();
        }
        object.remove("mapperVersion");
        object.put("mapperVersion", mapperVersion);
        PrintWriter writer = new PrintWriter(file);
        writer.println(new BeautifulJson().beautiful(object.toString()));
        writer.flush();
        writer.close();
    }

    /**
     * If the file doesn't exist, create it. If it does exist, delete it and create it. Then, write the JSON to the file
     *
     * @param file The file to save the JSON to.
     */
    public void save(File file) throws IOException {
        if(!file.exists()) {
            file.createNewFile();
        } else {
            file.delete();
            file.createNewFile();
        }
        object.remove("mapperVersion");
        object.put("mapperVersion", mapperVersion);
        PrintWriter writer = new PrintWriter(file);
        writer.println(new BeautifulJson().beautiful(object.toString()));
        writer.flush();
        writer.close();
    }

    /**
     * Removes the key and its corresponding value from this object
     *
     * @param key The key of the object to remove.
     */
    public void remove(String key) {
        object.remove(key);
    }

    /**
     * This function puts a key and value into the object.
     *
     * @param key The key to store the value under.
     * @param value The value to be stored in the map.
     */
    public void put(String key, Object value) {
        object.put(key, value);
    }

    /**
     * Get the value of the key as a JSONObject.
     *
     * @param key The key of the JSONObject to get.
     * @return A JSONObject
     */
    public JSONObject get(String key) {
        return object.getJSONObject(key);
    }

    /**
     * Get the string value of the key in the JSONObject.
     *
     * @param key The key to get the value of.
     * @return A string
     */
    public String getString(String key) {
        return object.getString(key);
    }

    /**
     * This function returns the value of the key as an integer.
     *
     * @param key The key to get the value from
     * @return The value of the key in the JSONObject object.
     */
    public int getInt(String key) {
        return object.getInt(key);
    }

    /**
     * > Returns the value mapped by name if it exists and is a boolean or can be coerced to a boolean, or false otherwise
     *
     * @param key The key to get the value of.
     * @return A boolean value
     */
    public boolean getBoolean(String key) {
        return object.getBoolean(key);
    }

    /**
     * > Returns the value mapped by name if it exists and is a double or can be coerced to a double, or 0.0 otherwise
     *
     * @param key The key to get the value from
     * @return A double value
     */
    public double getDouble(String key) {
        return object.getDouble(key);
    }

    /**
     * > Returns the value mapped by name if it exists and is a long or can be coerced to a long, or 0 otherwise
     *
     * @param key The key to get the value for.
     * @return The value of the key as a long.
     */
    public long getLong(String key) {
        return object.getLong(key);
    }

    /**
     * If the value is a double, return it as a float.
     *
     * @param key The key to get the value from
     * @return A float value.
     */
    public float getFloat(String key) {
        return (float) object.getDouble(key);
    }

    /**
     * If the value is a number, return it as a short.
     *
     * @param key The key to get the value from
     * @return A short
     */
    public short getShort(String key) {
        return (short) object.getInt(key);
    }

    /**
     * If the value is not a byte, then we'll try to convert it to a byte.
     *
     * @param key The key to get the value from
     * @return A byte
     */
    public byte getByte(String key) {
        return (byte) object.getInt(key);
    }

    /**
     * If the key exists, return the first character of the value, otherwise return null.
     *
     * @param key The key of the value you want to get.
     * @return A character
     */
    public char getChar(String key) {
        return object.getString(key).charAt(0);
    }

    /**
     * Returns true if the object has a value for the given key
     *
     * @param key The key to check for.
     * @return A boolean value.
     */
    public boolean has(String key) {
        return object.has(key);
    }

    /**
     * Returns true if the value associated with the given key is null
     *
     * @param key The key to check.
     * @return A boolean value.
     */
    public boolean isNull(String key) {
        return object.isNull(key);
    }

    /**
     * If the value of the key is a string, return true, otherwise return false.
     *
     * @param key The key of the value you want to check.
     * @return A boolean value.
     */
    public boolean isString(String key) {
        return object.get(key) instanceof String;
    }

    /**
     * If the value of the key is an instance of Integer, return true, otherwise return false.
     *
     * @param key The key of the value you want to check.
     * @return The method is returning a boolean value.
     */
    public boolean isInt(String key) {
        return object.get(key) instanceof Integer;
    }

    /**
     * If the value of the key is an instance of Boolean, then return true, otherwise return false.
     *
     * @param key The key of the value you want to check.
     * @return The method isBoolean() returns a boolean value.
     */
    public boolean isBoolean(String key) {
        return object.get(key) instanceof Boolean;
    }

    /**
     * If the value of the key is an instance of Double, return true, otherwise return false.
     *
     * @param key The key of the value you want to check.
     * @return A boolean value.
     */
    public boolean isDouble(String key) {
        return object.get(key) instanceof Double;
    }

    /**
     * If the value of the key is an instance of Long, return true, otherwise return false.
     *
     * @param key The key of the value you want to check.
     * @return A boolean value.
     */
    public boolean isLong(String key) {
        return object.get(key) instanceof Long;
    }

    /**
     * If the value of the key is an instance of Float, return true, otherwise return false.
     *
     * @param key The key of the value you want to check.
     * @return The method isFloat() returns a boolean value.
     */
    public boolean isFloat(String key) {
        return object.get(key) instanceof Float;
    }

    /**
     * If the value of the key is an instance of Short, return true, otherwise return false.
     *
     * @param key The key to check
     * @return A boolean value.
     */
    public boolean isShort(String key) {
        return object.get(key) instanceof Short;
    }

    /**
     * If the value of the key is an instance of Byte, return true, otherwise return false.
     *
     * @param key The key of the value you want to check.
     * @return A boolean value.
     */
    public boolean isByte(String key) {
        return object.get(key) instanceof Byte;
    }


    /**
     * If the value of the key is an instance of the Character class, then return true.
     *
     * @param key The key of the value you want to check.
     * @return A boolean value.
     */
    public boolean isChar(String key) {
        return object.get(key) instanceof Character;
    }

    /**
     * If the value of the key is an instance of JSONObject, then return true
     *
     * @param key The key of the object you want to check.
     * @return A boolean value.
     */
    public boolean isObject(String key) {
        return object.get(key) instanceof JSONObject;
    }

    /**
     * If the value of the key is an instance of JSONArray, then return true
     *
     * @param key The key of the object you want to check.
     * @return A boolean value.
     */
    public boolean isArray(String key) {
        return object.get(key) instanceof JSONArray;
    }

    /**
     * If the value of the key is a number, return true, otherwise return false.
     *
     * @param key The key to check.
     * @return A boolean value.
     */
    public boolean isNumber(String key) {
        return object.get(key) instanceof Number;
    }

    /**
     * If the value is a string, integer, boolean, double, long, float, short, byte, or character, then it's a primitive.
     *
     * @param key The key of the value you want to check.
     * @return A boolean value.
     */
    public boolean isPrimitive(String key) {
        return object.get(key) instanceof String || object.get(key) instanceof Integer || object.get(key) instanceof Boolean || object.get(key) instanceof Double || object.get(key) instanceof Long || object.get(key) instanceof Float || object.get(key) instanceof Short || object.get(key) instanceof Byte || object.get(key) instanceof Character;
    }

    /**
     * It returns true if the value of the key is an array of primitive types
     *
     * @param key The key of the value you want to check.
     * @return A boolean value.
     */
    public boolean isPrimitiveArray(String key) {
        return object.get(key) instanceof String[] || object.get(key) instanceof Integer[] || object.get(key) instanceof Boolean[] || object.get(key) instanceof Double[] || object.get(key) instanceof Long[] || object.get(key) instanceof Float[] || object.get(key) instanceof Short[] || object.get(key) instanceof Byte[] || object.get(key) instanceof Character[];
    }

    /**
     * If the value of the key is an array of strings, return true.
     *
     * @param key The key of the value you want to check.
     * @return A boolean value.
     */
    public boolean isStringArray(String key) {
        return object.get(key) instanceof String[];
    }

    /**
     * If the value of the key is an array of integers, return true.
     *
     * @param key The key of the value you want to check.
     * @return The method is returning a boolean value.
     */
    public boolean isIntArray(String key) {
        return object.get(key) instanceof Integer[];
    }

    /**
     * Returns true if the value associated with the given key is an array of booleans.
     *
     * @param key The key of the value you want to check.
     * @return A boolean value.
     */
    public boolean isBooleanArray(String key) {
        return object.get(key) instanceof Boolean[];
    }

    /**
     * If the value of the key is an array of doubles, return true.
     *
     * @param key The key of the value you want to check.
     * @return A boolean value.
     */
    public boolean isDoubleArray(String key) {
        return object.get(key) instanceof Double[];
    }

    /**
     * If the value of the key is an array of longs, return true.
     *
     * @param key The key of the value you want to check.
     * @return A boolean value.
     */
    public boolean isLongArray(String key) {
        return object.get(key) instanceof Long[];
    }

    /**
     * Returns true if the value associated with the given key is an array of floats.
     *
     * @param key The key of the value you want to check.
     * @return A boolean value.
     */
    public boolean isFloatArray(String key) {
        return object.get(key) instanceof Float[];
    }

    /**
     * Returns true if the value at the given key is an array of shorts.
     *
     * @param key The key of the value you want to check.
     * @return A boolean value.
     */
    public boolean isShortArray(String key) {
        return object.get(key) instanceof Short[];
    }

    /**
     * If the value of the key is an instance of Byte[], then return true.
     *
     * @param key The key of the value you want to check.
     * @return A boolean value.
     */
    public boolean isByteArray(String key) {
        return object.get(key) instanceof Byte[];
    }

    /**
     * If the value of the key is an array of characters, return true.
     *
     * @param key The key of the value you want to check.
     * @return A boolean value.
     */
    public boolean isCharArray(String key) {
        return object.get(key) instanceof Character[];
    }

    /**
     * If the value of the key is an array of JSONObjects, then return true
     *
     * @param key The key of the value you want to check.
     * @return A boolean value.
     */
    public boolean isObjectArray(String key) {
        return object.get(key) instanceof JSONObject[];
    }

    /**
     * Returns true if the value associated with the given key is an array of numbers.
     *
     * @param key The key of the value you want to check.
     * @return A boolean value.
     */
    public boolean isNumberArray(String key) {
        return object.get(key) instanceof Number[];
    }

    /**
     * Get the JSONArray with the given key, then create a String array of the same length, then loop through the JSONArray
     * and add each element to the String array, then return the String array.
     *
     * @param key The key of the array you want to get.
     * @return An array of strings.
     */
    public String[] getStringArray(String key) {
        JSONArray array = object.getJSONArray(key);
        String[] strings = new String[array.length()];
        for(int i = 0; i < array.length(); i++) {
            strings[i] = array.getString(i);
        }
        return strings;
    }

    /**
     * It takes a JSONArray and returns an int array
     *
     * @param key The key of the array you want to get.
     * @return An array of integers.
     */
    public int[] getIntArray(String key) {
        JSONArray array = object.getJSONArray(key);
        int[] ints = new int[array.length()];
        for(int i = 0; i < array.length(); i++) {
            ints[i] = array.getInt(i);
        }
        return ints;
    }

    /**
     * It gets a JSONArray from the JSONObject, converts it to a boolean array, and returns it
     *
     * @param key The key of the value you want to get.
     * @return An array of booleans.
     */
    public boolean[] getBooleanArray(String key) {
        JSONArray array = object.getJSONArray(key);
        boolean[] booleans = new boolean[array.length()];
        for(int i = 0; i < array.length(); i++) {
            booleans[i] = array.getBoolean(i);
        }
        return booleans;
    }

    /**
     * It gets a JSONArray from the JSONObject, then loops through the array and adds each element to a double array
     *
     * @param key The key of the array you want to get.
     * @return An array of doubles.
     */
    public double[] getDoubleArray(String key) {
        JSONArray array = object.getJSONArray(key);
        double[] doubles = new double[array.length()];
        for(int i = 0; i < array.length(); i++) {
            doubles[i] = array.getDouble(i);
        }
        return doubles;
    }

    /**
     * It gets a JSONArray from the JSONObject, then loops through the array and adds each value to a long array
     *
     * @param key The key of the value you want to get.
     * @return An array of longs
     */
    public long[] getLongArray(String key) {
        JSONArray array = object.getJSONArray(key);
        long[] longs = new long[array.length()];
        for(int i = 0; i < array.length(); i++) {
            longs[i] = array.getLong(i);
        }
        return longs;
    }

    /**
     * It gets a JSONArray from the JSONObject, then loops through the array and adds each element to a float array
     *
     * @param key The key of the array you want to get.
     * @return An array of floats.
     */
    public float[] getFloatArray(String key) {
        JSONArray array = object.getJSONArray(key);
        float[] floats = new float[array.length()];
        for(int i = 0; i < array.length(); i++) {
            floats[i] = (float) array.getDouble(i);
        }
        return floats;
    }

    /**
     * Get the JSONArray with the given key, convert it to a short array, and return it.
     *
     * @param key The key of the value you want to get.
     * @return An array of shorts
     */
    public short[] getShortArray(String key) {
        JSONArray array = object.getJSONArray(key);
        short[] shorts = new short[array.length()];
        for(int i = 0; i < array.length(); i++) {
            shorts[i] = (short) array.getInt(i);
        }
        return shorts;
    }

    /**
     * It converts a JSONArray of integers into a byte array
     *
     * @param key The key of the value you want to get.
     * @return A byte array.
     */
    public byte[] getByteArray(String key) {
        JSONArray array = object.getJSONArray(key);
        byte[] bytes = new byte[array.length()];
        for(int i = 0; i < array.length(); i++) {
            bytes[i] = (byte) array.getInt(i);
        }
        return bytes;
    }

    /**
     * It converts a JSONArray of integers into a char array
     *
     * @param key The key of the array you want to get.
     * @return A char array
     */
    public char[] getCharArray(String key) {
        JSONArray array = object.getJSONArray(key);
        char[] chars = new char[array.length()];
        for(int i = 0; i < array.length(); i++) {
            chars[i] = (char) array.getInt(i);
        }
        return chars;
    }

    /**
     * It takes a key, gets the JSONArray from the object, creates a new array of JSONObjects, and then loops through the
     * JSONArray and adds each JSONObject to the new array
     *
     * @param key The key of the array you want to get.
     * @return An array of JSONObjects
     */
    public JSONObject[] getObjectArray(String key) {
        JSONArray array = object.getJSONArray(key);
        JSONObject[] objects = new JSONObject[array.length()];
        for(int i = 0; i < array.length(); i++) {
            objects[i] = new JSONObject(array.getJSONObject(i));
        }
        return objects;
    }

    /**
     * Get the JSONArray at the given key, then iterate through it and cast each element to a Number, then return the array
     * of Numbers.
     *
     * @param key The key of the array you want to get.
     * @return An array of numbers.
     */
    public Number[] getNumberArray(String key) {
        JSONArray array = object.getJSONArray(key);
        Number[] numbers = new Number[array.length()];
        for(int i = 0; i < array.length(); i++) {
            numbers[i] = (Number) array.get(i);
        }
        return numbers;
    }

    /**
     * This function takes a key as a parameter, and returns the value of that key as a JSONArray
     *
     * @param key The key of the JSONObject you want to get the array from.
     * @return A JSONArray object.
     */
    public JSONArray getArray(String key) {
        return object.getJSONArray(key);
    }

}