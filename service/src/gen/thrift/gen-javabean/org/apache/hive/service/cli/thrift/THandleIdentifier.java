/**
 * Autogenerated by Thrift Compiler (0.7.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package org.apache.hive.service.cli.thrift;

import org.apache.commons.lang.builder.HashCodeBuilder;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class THandleIdentifier implements org.apache.thrift.TBase<THandleIdentifier, THandleIdentifier._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("THandleIdentifier");

  private static final org.apache.thrift.protocol.TField GUID_FIELD_DESC = new org.apache.thrift.protocol.TField("guid", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField SECRET_FIELD_DESC = new org.apache.thrift.protocol.TField("secret", org.apache.thrift.protocol.TType.STRING, (short)2);

  private ByteBuffer guid; // required
  private ByteBuffer secret; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    GUID((short)1, "guid"),
    SECRET((short)2, "secret");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // GUID
          return GUID;
        case 2: // SECRET
          return SECRET;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments

  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.GUID, new org.apache.thrift.meta_data.FieldMetaData("guid", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));
    tmpMap.put(_Fields.SECRET, new org.apache.thrift.meta_data.FieldMetaData("secret", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(THandleIdentifier.class, metaDataMap);
  }

  public THandleIdentifier() {
  }

  public THandleIdentifier(
    ByteBuffer guid,
    ByteBuffer secret)
  {
    this();
    this.guid = guid;
    this.secret = secret;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public THandleIdentifier(THandleIdentifier other) {
    if (other.isSetGuid()) {
      this.guid = org.apache.thrift.TBaseHelper.copyBinary(other.guid);
;
    }
    if (other.isSetSecret()) {
      this.secret = org.apache.thrift.TBaseHelper.copyBinary(other.secret);
;
    }
  }

  public THandleIdentifier deepCopy() {
    return new THandleIdentifier(this);
  }

  @Override
  public void clear() {
    this.guid = null;
    this.secret = null;
  }

  public byte[] getGuid() {
    setGuid(org.apache.thrift.TBaseHelper.rightSize(guid));
    return guid == null ? null : guid.array();
  }

  public ByteBuffer bufferForGuid() {
    return guid;
  }

  public void setGuid(byte[] guid) {
    setGuid(guid == null ? (ByteBuffer)null : ByteBuffer.wrap(guid));
  }

  public void setGuid(ByteBuffer guid) {
    this.guid = guid;
  }

  public void unsetGuid() {
    this.guid = null;
  }

  /** Returns true if field guid is set (has been assigned a value) and false otherwise */
  public boolean isSetGuid() {
    return this.guid != null;
  }

  public void setGuidIsSet(boolean value) {
    if (!value) {
      this.guid = null;
    }
  }

  public byte[] getSecret() {
    setSecret(org.apache.thrift.TBaseHelper.rightSize(secret));
    return secret == null ? null : secret.array();
  }

  public ByteBuffer bufferForSecret() {
    return secret;
  }

  public void setSecret(byte[] secret) {
    setSecret(secret == null ? (ByteBuffer)null : ByteBuffer.wrap(secret));
  }

  public void setSecret(ByteBuffer secret) {
    this.secret = secret;
  }

  public void unsetSecret() {
    this.secret = null;
  }

  /** Returns true if field secret is set (has been assigned a value) and false otherwise */
  public boolean isSetSecret() {
    return this.secret != null;
  }

  public void setSecretIsSet(boolean value) {
    if (!value) {
      this.secret = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case GUID:
      if (value == null) {
        unsetGuid();
      } else {
        setGuid((ByteBuffer)value);
      }
      break;

    case SECRET:
      if (value == null) {
        unsetSecret();
      } else {
        setSecret((ByteBuffer)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case GUID:
      return getGuid();

    case SECRET:
      return getSecret();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case GUID:
      return isSetGuid();
    case SECRET:
      return isSetSecret();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof THandleIdentifier)
      return this.equals((THandleIdentifier)that);
    return false;
  }

  public boolean equals(THandleIdentifier that) {
    if (that == null)
      return false;

    boolean this_present_guid = true && this.isSetGuid();
    boolean that_present_guid = true && that.isSetGuid();
    if (this_present_guid || that_present_guid) {
      if (!(this_present_guid && that_present_guid))
        return false;
      if (!this.guid.equals(that.guid))
        return false;
    }

    boolean this_present_secret = true && this.isSetSecret();
    boolean that_present_secret = true && that.isSetSecret();
    if (this_present_secret || that_present_secret) {
      if (!(this_present_secret && that_present_secret))
        return false;
      if (!this.secret.equals(that.secret))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();

    boolean present_guid = true && (isSetGuid());
    builder.append(present_guid);
    if (present_guid)
      builder.append(guid);

    boolean present_secret = true && (isSetSecret());
    builder.append(present_secret);
    if (present_secret)
      builder.append(secret);

    return builder.toHashCode();
  }

  public int compareTo(THandleIdentifier other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    THandleIdentifier typedOther = (THandleIdentifier)other;

    lastComparison = Boolean.valueOf(isSetGuid()).compareTo(typedOther.isSetGuid());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetGuid()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.guid, typedOther.guid);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetSecret()).compareTo(typedOther.isSetSecret());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSecret()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.secret, typedOther.secret);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    org.apache.thrift.protocol.TField field;
    iprot.readStructBegin();
    while (true)
    {
      field = iprot.readFieldBegin();
      if (field.type == org.apache.thrift.protocol.TType.STOP) { 
        break;
      }
      switch (field.id) {
        case 1: // GUID
          if (field.type == org.apache.thrift.protocol.TType.STRING) {
            this.guid = iprot.readBinary();
          } else { 
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: // SECRET
          if (field.type == org.apache.thrift.protocol.TType.STRING) {
            this.secret = iprot.readBinary();
          } else { 
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
          }
          break;
        default:
          org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
      }
      iprot.readFieldEnd();
    }
    iprot.readStructEnd();
    validate();
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    validate();

    oprot.writeStructBegin(STRUCT_DESC);
    if (this.guid != null) {
      oprot.writeFieldBegin(GUID_FIELD_DESC);
      oprot.writeBinary(this.guid);
      oprot.writeFieldEnd();
    }
    if (this.secret != null) {
      oprot.writeFieldBegin(SECRET_FIELD_DESC);
      oprot.writeBinary(this.secret);
      oprot.writeFieldEnd();
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("THandleIdentifier(");
    boolean first = true;

    sb.append("guid:");
    if (this.guid == null) {
      sb.append("null");
    } else {
      org.apache.thrift.TBaseHelper.toString(this.guid, sb);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("secret:");
    if (this.secret == null) {
      sb.append("null");
    } else {
      org.apache.thrift.TBaseHelper.toString(this.secret, sb);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (!isSetGuid()) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'guid' is unset! Struct:" + toString());
    }

    if (!isSetSecret()) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'secret' is unset! Struct:" + toString());
    }

  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

}

