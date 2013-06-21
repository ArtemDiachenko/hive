/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hive.serde2.lazy;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Timestamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.serde2.io.TimestampWritable;
import org.apache.hadoop.hive.serde2.lazy.objectinspector.primitive.LazyTimestampObjectInspector;

/**
 *
 * LazyTimestamp.
 * Serializes and deserializes a Timestamp in the JDBC timestamp format
 *
 *    YYYY-MM-DD HH:MM:SS.[fff...]
 *
 */
public class LazyTimestamp extends LazyPrimitive<LazyTimestampObjectInspector, TimestampWritable> {
  static final private Log LOG = LogFactory.getLog(LazyTimestamp.class);
  static final private int NANOSECONDS_PERSEC=1000000000;
  static final private BigDecimal NANOSECONDS_PERSEC_BD = new BigDecimal(NANOSECONDS_PERSEC);

  public LazyTimestamp(LazyTimestampObjectInspector oi) {
    super(oi);
    data = new TimestampWritable();
  }

  public LazyTimestamp(LazyTimestamp copy) {
    super(copy);
    data = new TimestampWritable(copy.data);
  }

  /**
   * Initilizes LazyTimestamp object by interpreting the input bytes
   * as a JDBC timestamp string
   *
   * @param bytes
   * @param start
   * @param length
   */
  @Override
  public void init(ByteArrayRef bytes, int start, int length) {
    String s = null;
    try {
      s = new String(bytes.getData(), start, length, "US-ASCII");
    } catch (UnsupportedEncodingException e) {
      LOG.error(e);
      s = "";
    }

    Timestamp t = null;
    if (s.compareTo("NULL") == 0) {
      isNull = true;
      logExceptionMessage(bytes, start, length, "TIMESTAMP");
    } else {
      // Supported timestamp formats:
      // 1. Integer: UNIX epoch seconds
      // 2. Floating point: UNIX epoch seconds plus nanoseconds
      // 3. Strings: JDBC compliant java.sql.Timestamp format
      //      "YYYY-MM-DD HH:MM:SS.fffffffff" (9 decimal place precision)

      // Assume the given format is JDBC compliant and try to convert
      // if it fails fall back to conversion from other two formats
      try {
        t = Timestamp.valueOf(s);
      } catch (IllegalArgumentException e) {
        try {
          BigDecimal value = new BigDecimal(s);
          t = new Timestamp(value.longValue()*1000);

          // if the value has any decimal part process it to get the nanoseconds part
          if (value.scale()>0) {
            if (value.scale()>9) {
              throw new NumberFormatException("");
            }

            // convert the decimal part of the seconds into nanoseconds
            value = value.subtract(new BigDecimal(value.longValue()));
            value = value.multiply(NANOSECONDS_PERSEC_BD);

            // Truncate the nanoseconds to 999,999,999.
            // Timestamp.setNanos() accepts values between 0-999,999,999
            t.setNanos(Math.min(value.intValue(), NANOSECONDS_PERSEC-1));
          }
        } catch(Exception ex) {
          isNull = true;
          logExceptionMessage(bytes, start, length, "TIMESTAMP");
        }
      }
    }
    data.set(t);
  }

  private static final String nullTimestamp = "NULL";

  /**
   * Writes a Timestamp in JDBC timestamp format to the output stream
   * @param out
   *          The output stream
   * @param i
   *          The Timestamp to write
   * @throws IOException
   */
  public static void writeUTF8(OutputStream out, TimestampWritable i)
      throws IOException {
    if (i == null) {
      // Serialize as time 0
      out.write(TimestampWritable.nullBytes);
    } else {
      out.write(i.toString().getBytes("US-ASCII"));
    }
  }

  @Override
  public TimestampWritable getWritableObject() {
    return data;
  }
}
