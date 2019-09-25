package org.daisleyharrison.security.samples.jerseyService.utilities;
import java.nio.CharBuffer;

import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.bind.adapter.JsonbAdapter;

/**
 * This JsonAdaper for passwords
 * ensures that the password is not interned as a java String
 * passwords should ALWAYS be stored in char arrays
 */
public class PasswordAdapter 
implements JsonbAdapter<char[], JsonValue> {
  private class JsonStringImpl implements JsonString {
    private char[] value;
    public JsonStringImpl( char[] value) {
      this.value = value;
    }
    @Override 
    public CharSequence getChars() {
      return CharBuffer.wrap(this.value);
    }
    @Override
    public String getString() {
      return new String(this.value);
    }
    @Override
    public ValueType getValueType() {
      return ValueType.STRING;
    }
  }
  @Override
  public JsonValue adaptToJson(char[] password) {
      return new JsonStringImpl(password);
  }

  @Override
  public char[] adaptFromJson(JsonValue json) {
      if(json instanceof JsonString) {
        JsonString jsonString = (JsonString)json;
        CharSequence charSequence = jsonString.getChars();
        char[] chars = new char[charSequence.length()];
        for(int i = 0; i < chars.length; i++) {
          chars[i] = charSequence.charAt(i);
        }
        return chars;
      }
      else {
        return new char[0];
      }
  }
}