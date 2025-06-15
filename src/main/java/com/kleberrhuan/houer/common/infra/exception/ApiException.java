/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.exception;

import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import java.io.Serial;
import lombok.Getter;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApiException
  extends RuntimeException
  implements MessageSourceResolvable {

  @Serial
  private static final long serialVersionUID = 1L;

  private final HttpStatus status;
  private final ApiErrorType errorType;
  private final transient MessageKey key;
  private final transient Object[] args;

  protected ApiException(
    HttpStatus status,
    ApiErrorType errorType,
    MessageKey key,
    Object... args
  ) {
    super(key.base());
    this.status = status;
    this.errorType = errorType;
    this.key = key;
    this.args = args;
  }

  @Override
  public String[] getCodes() {
    return new String[] { key.detailCode(), key.messageCode() };
  }

  @Override
  public Object[] getArguments() {
    return args;
  }

  @Override
  public String getDefaultMessage() {
    return key.base();
  }

  @Serial
  private void writeObject(java.io.ObjectOutputStream oos)
    throws java.io.IOException {
    oos.defaultWriteObject();
  }

  @Serial
  private void readObject(java.io.ObjectInputStream ois)
    throws java.io.IOException, ClassNotFoundException {
    ois.defaultReadObject();
  }
}
