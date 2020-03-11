package cn.xpbootcamp.legacy_code.enums;

import lombok.Getter;

@Getter
public enum CheckResult {
  SUCCESS(true), FAILED(false), NO_RESULT(false);
  private boolean success;

  CheckResult(boolean success) {
    this.success = success;
  }
}
