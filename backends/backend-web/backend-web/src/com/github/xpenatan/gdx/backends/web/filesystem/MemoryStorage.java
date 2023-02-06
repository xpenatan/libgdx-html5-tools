package com.github.xpenatan.gdx.backends.web.filesystem;

import com.badlogic.gdx.utils.ObjectMap;
import org.teavm.jso.browser.Storage;

import java.util.ArrayList;
import java.util.List;

/**
 * Storage for data in memory.
 *
 * @author noblemaster
 */
class MemoryStorage extends Storage {

  /** Contains all the data. */
  private final List<String> keys;
  private final ObjectMap<String, String> map;


  MemoryStorage() {
    keys = new ArrayList<String>(16);
    map = new ObjectMap<String, String>(16);
  }

  /** Removes large files as needed to prevent out of memory problems. */
  void cleanup() {
    // remove large files if we are above max. bytes
    long maxChars = 10000000;
    boolean cleaned = true;
    while  (cleaned) {
      // calculate new total
      long total = 0;
      for (String key: keys) {
        total += map.get(key).length();
      }

      // over max?
      if (total > maxChars) {
        // remove the next largest file
        String largeKey = null;
        long largeKeyLength = -1;
        for (String key: keys) {
          long length = map.get(key).length();
          if (length > largeKeyLength) {
            largeKey = key;
            largeKeyLength = length;
          }
        }

        // and clean it..
        removeItem(largeKey);
        cleaned = true;
      }
      else {
        // so far so good
        cleaned = false;
      }
    }
  }

  @Override
  public int getLength() {
    return keys.size();
  }

  @Override
  public String key(int i) {
    return map.get(keys.get(i));
  }

  @Override
  public String getItem(String key) {
    return map.get(key);
  }

  @Override
  public void setItem(String key, String item) {
    if (!map.containsKey(key)) {
      keys.add(key);
    }
    map.put(key, item);
  }

  @Override
  public void removeItem(String key) {
    keys.remove(key);
    map.remove(key);
  }

  @Override
  public void clear() {
    keys.clear();
    map.clear();
  }
}
