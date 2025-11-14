package com.codelry.util.generator.generator;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;

public class PadFilter implements Filter {
  public static final String NAME = "padint";

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... args) {
    int length;
    if (args.length > 0 && args[0] != null) {
      length = Integer.parseInt(args[0]);
    } else {
      length = 10;
    }
    return String.format("%0" + length + "d", Integer.parseInt(object.toString()));
  }

  @Override
  public String getName() {
    return NAME;
  }
}
