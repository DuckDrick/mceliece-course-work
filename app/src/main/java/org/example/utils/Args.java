package org.example.utils;

import com.beust.jcommander.Parameter;

public class Args {

  @Parameter(names = { "-name" })
  public String attackName = "lbs";

  @Parameter(names = { "-m" })
  public Integer m = 5;

  @Parameter(names = { "-t" })
  public Integer t = 2;

  @Parameter(names = { "-p" })
  public Integer p = 1;

  @Parameter(names = { "-tries" })
  public Integer tries = 7;

  @Parameter(names = { "-seed" })
  public String seed = "SomeRandomSeed";

  @Parameter(names = { "-retry" })
  public Boolean shouldRetry = false;

  @Parameter(names = { "-lbsTries" })
  public Integer lbsTries = 1000;

}
