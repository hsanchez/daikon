#!/usr/bin/env perl

# Builds and tests the software in the Daikon CVS repository.  If the
# --quiet option is selected, only generates output if a task fails
# (useful for cron job).  If the --nocleanup option is selected, does
# not remove generated files (good for debugging).

use strict;
use English;
$WARNING = 1;
use Cwd;

# Process the command-line args
my $usage = "Usage: buildtest.pl [--quiet]\n"
  . "  Debugging flags:  [--nocleanup] [--skip_daikon] [--skip_dfec] [--skip_dfej] [--use_ver3]\n";
my $quiet = 0;
my $nocleanup = 0;
# These three flags permit only part of the tests to be run; good for debugging.
my $skip_daikon = 0;
my $skip_dfec = 0;
my $skip_dfej = 0;
my $use_ver3 = 0;
while (scalar(@ARGV) > 0) {
  my $arg = shift @ARGV;
  if ($arg eq "--quiet") {
    $quiet = 1;
  } elsif ($arg eq "--nocleanup") {
    $nocleanup = 1;
  } elsif ($arg eq "--skip_daikon") {
    $skip_daikon = 1;
  } elsif ($arg eq "--skip_dfec") {
    $skip_dfec = 1;
  } elsif ($arg eq "--skip_dfej") {
    $skip_dfej = 1;
  } elsif ($arg eq "--use_ver3") {
# When on, use version 3 of Daikon
    $use_ver3 = 1;
  } else {
    die "$usage\n";
  }
}

# Set the DAIKONPARENT variable
my $date = `date +%Y%m%d-%H%M%S`;
#my $date = 'dummy-date';
chomp $date;
my $DAIKONPARENT = cwd() . "/$date";
$ENV{"DAIKONPARENT"} = $DAIKONPARENT;

# Set other initial variables
my $CVS_REP = "/g4/projects/invariants/.CVS/";
my $CVS_TAG = "ENGINE_V2_PATCHES";
$ENV{"JAVAC"} = "javac -g";

# Whether or not to run Make in two-job mode
# my $J2 = "-j2";
my $J2 = "";

# Run java using the -classic switch, to workaround JVM exit deadlock
# bug.  The bug is present in version 1.3.1_02; have not tested with 1.4.
# A similar bug has been reported against solaris:
# http://developer.java.sun.com/developer/bugParade/bugs/4305128.html

# Commented out 2002-Aug-13 because -classic apparently causes outOfMemoryError's
# my $RUN_JAVA = '\'java -classic -Xmx256m\'';

# The success of each step in the build/test process
my %success = ();

mkdir($DAIKONPARENT, 0777) or die "can't make directory $DAIKONPARENT: $!\n";
chdir($DAIKONPARENT) or die "can't chdir to $DAIKONPARENT: $!\n";

my $LOG = "buildtest.out";

$success{"daikon_checkout"} = daikon_checkout();

# Inherit the environment of the group-wide init file
if ($success{"daikon_checkout"}) {
  %ENV = get_env("$DAIKONPARENT/invariants/scripts/pag-daikon.bashrc");
}
my $INV = $ENV{"INV"};

if ($success{"daikon_checkout"}) {
  $success{"daikon_update"} = daikon_update();
  $success{"tests_update"} = tests_update();
}
if ($success{"daikon_update"}) {
  $success{"daikon_compile"} = daikon_compile();
}

if (! $skip_daikon) {
  if ($success{"daikon_compile"}
      && $success{"tests_update"}) {
    $success{"daikon_unit_test"} = daikon_unit_test();
    $success{"daikon_system_test"} = daikon_system_test();
    $success{"diff_system_test"} = diff_system_test();
  }
}

if (! $skip_dfec) {
  if ($success{"daikon_checkout"}
      && $success{"tests_update"}) {
    $success{"dfec_system_test"} = dfec_system_test();
  }
}

if (! $skip_dfej) {
  $success{"dfej_checkout"} = dfej_checkout();
  if ($success{"dfej_checkout"}) {
    $success{"dfej_configure"} = dfej_configure();
  }
  if ($success{"dfej_configure"}) {
    $success{"dfej_compile"} = dfej_compile();
  }
}

# Print the output files for any steps that failed.  Output steps are
# sorted alphabetically, not in order of operation.  This is OK, since
# failures should occur rarely, and multiple failures even more
# rarely.
my @failed_steps = ();
foreach my $step (sort keys %success) {
  if (!$success{$step}) {
    push @failed_steps, $step;
  }
}
if (@failed_steps != 0) {
  print_log("\n\n");
  foreach my $step (@failed_steps) {
    if (-e "${step}_summary.out") {
      print_log("*** ${step}_summary.out ***\n");
      print_log(`cat ${step}_summary.out`);
      print_log("\n\n");
    } elsif (-e "$step.out") {
      print_log("*** $step.out ***\n");
      print_log(`cat $step.out`);
      print_log("\n\n");
    } elsif (-e "${step}_clean.out") {
      print_log("*** ${step}_clean.out ***\n");
      print_log(`cat ${step}_clean.out`);
      print_log("\n\n");
    } else {
      print_log("*** $step ***\n");
      print_log("<no output file>");
      print_log("\n\n");
    }
  }

  # If quiet is not set, this output was printed earlier.  If quiet is
  # set, the output was instead written to a log file (see function
  # print_log).  If a step failed, we must print the log file now.  To
  # summarize, if a step fails, the same output is printed whether or
  # not quiet is set.  However, if all steps succeed, there is no
  # output iff quiet is set.
  if ($quiet) {
    open LOG, $LOG or die "can't open $LOG: $!\n";
    my $log = join('', <LOG>);
    print $log;
  }
}


# Move the .diff files to another directory, then remove the source
# checkouts
mkdir("diffs", 0777) or die "can't make directory diffs: $!\n";

foreach my $subdir ("daikon", "diff", "dfec") {
  mkdir("diffs/$subdir", 0777) or die "can't make directory diffs/$subdir: $!\n";
  my $diffs = `find invariants/tests/$subdir-tests -name "*.diff"`;
  foreach my $file (split '\n',$diffs) {
    `cp -p $file diffs/$subdir`;
    die "can't copy diff file $file to diffs/$subdir\n" if ($CHILD_ERROR);
  }
}

if (! $nocleanup) {
  `rm -rf dfej invariants`;
}

exit();


# SUBROUTINES

# Check the invariants module out from CVS
sub daikon_checkout {
  print_log("Checking out Daikon...");
  `cvs -d $CVS_REP co invariants &> daikon_checkout.out`;
  if ($CHILD_ERROR) {
    print_log("FAILED\n");
    return 0;
  } else {
    print_log("OK\n");
    return 1;
  }
}


# Check the dfej module out from CVS
sub dfej_checkout {
  print_log("Checking out dfej...");
  `cvs -d $CVS_REP co dfej &> dfej_checkout.out`;
  if ($CHILD_ERROR) {
    print_log("FAILED\n");
    return 0;
  } else {
    print_log("OK\n");
    return 1;
  }
}


# Update the daikon directory to the ENGINE_V2_PATCHES tag
sub daikon_update {
  print_log("Updating Daikon...");
  my $daikon_dir = "invariants/java/daikon";
  chdir($daikon_dir) or die "can't chdir to $daikon_dir: $!\n";
  if ($use_ver3) {
      `cvs -d $CVS_REP up &> ../../../daikon_update.out`;
  } else {
      `cvs -d $CVS_REP up -r $CVS_TAG &> ../../../daikon_update.out`;
  }
  chdir($DAIKONPARENT) or die "can't chdir to $DAIKONPARENT: $!\n";
  if ($CHILD_ERROR) {
    print_log("FAILED\n");
    return 0;
  } else {
    print_log("OK\n");
    return 1;
  }
}


# Update the tests directory to the ENGINE_V2_PATCHES tag
sub tests_update {
  print_log("Updating tests...");
  my $tests_dir = "invariants/tests";
  chdir($tests_dir) or die "can't chdir to $tests_dir: $!\n";
  if ($use_ver3) {
      `cvs -d $CVS_REP up &> ../../tests_update.out`;
  } else {
      `cvs -d $CVS_REP up -r $CVS_TAG &> ../../tests_update.out`;
  }
  chdir($DAIKONPARENT) or die "can't chdir to $DAIKONPARENT: $!\n";
  if ($CHILD_ERROR) {
    print_log("FAILED\n");
    return 0;
  } else {
    print_log("OK\n");
    return 1;
  }
}


# Run 'configure' on dfej
sub dfej_configure {
  print_log("Configuring dfej...");
  chdir("dfej") or die "Can't chdir to dfej: $!\n";
  `./configure &> ../dfej_configure.out`;
  chdir($DAIKONPARENT) or die "Can't chdir to $DAIKONPARENT: $!\n";
  if ($CHILD_ERROR) {
    print_log("FAILED\n");
    return 0;
  } else {
    print_log("OK\n");
    return 1;
  }
}


# Compile daikon using javac
sub daikon_compile {
  print_log("Compiling Daikon...");
  `make -C $INV/java all_directly &> daikon_compile.out`;
  if ($CHILD_ERROR) {
    print_log("FAILED\n");
    return 0;
  } else {
    print_log("OK\n");
    return 1;
  }
}


# Compile dfej using gcc
sub dfej_compile {
  print_log("Compiling dfej...");
  `make $J2 -C dfej/src &> dfej_compile.out`;
  if ($CHILD_ERROR) {
    print_log("FAILED\n");
    return 0;
  } else {
    print_log("OK\n");
    return 1;
  }
}


# Run the daikon JUnit unit tests
sub daikon_unit_test {
  print_log("Daikon unit tests...");
  my $command = "make -C $INV/java/daikon junit " .
    "&> daikon_unit_test.out";
  `$command`;
  if ($CHILD_ERROR) {
    print_log("FAILED\n");
    return 0;
  } else {
    print_log("OK\n");
    return 1;
  }
}


# Run the daikon system tests.  Scan the output for any nonzero
# ".diff" filesizes.
sub daikon_system_test {
  # Standard test suite
  my $TEST_SUITE = "text-diff";
  # Short test suites
#  my $TEST_SUITE = "do-print_tokens-text-diff do-StreetNumberSet-text-diff";
  print_log("Daikon system tests...");

  my $command = "make -C $INV/tests/daikon-tests clean " .
    "&> daikon_system_test_clean.out";
  `$command`;
  if ($CHILD_ERROR) {
    print_log("FAILED\n");
    return 0;
  }

  $command = "make RUN_JAVA=$RUN_JAVA $J2 -C $INV/tests/daikon-tests " .
    "$TEST_SUITE &> daikon_system_test.out";
  `$command`;
  if ($CHILD_ERROR) {
    print_log("FAILED\n");
    return 0;
  }

  $command = "make -C $INV/tests/daikon-tests summary " .
    "2>&1 | tee daikon_system_test_summary.out";
  my $result = `$command`;
  if ($CHILD_ERROR) {
    print_log("FAILED\n");
    return 0;
  }

  foreach my $line (split /\n/,$result) {
    next if ($line =~ /^make/);
    if (!($line =~ /^0\s/)) {
      print_log("FAILED\n");
      return 0;
    }
  }

  print_log("OK\n");
  return 1;
}


# Run the diff system tests.  Scan the output for any "FAILED" tests.
sub diff_system_test {
  print_log("Diff system tests...");

  my $command = "make $J2 -C $INV/tests/diff-tests " .
    "&> diff_system_test.out";
  `$command`;
  if ($CHILD_ERROR) {
    print_log("FAILED\n");
    return 0;
  }

  $command = "make -C $INV/tests/diff-tests summary " .
    "2>&1 | tee diff_system_test_summary.out";
  my $result = `$command`;
  if ($CHILD_ERROR) {
    print_log("FAILED\n");
    return 0;
  }

  foreach my $line (split /\n/,$result) {
    next if ($line =~ /^make/);
    if (!($line =~ /^OK\s/)) {
      print_log("FAILED\n");
      return 0;
    }
  }

  print_log("OK\n");
  return 1;
}


# Run the dfec system tests.  Scans the output for any "FAILED" tests.
# Uses the version of dfec in invariants/front-end/c.  Could build
# dfec from source instead.
sub dfec_system_test {
  # Standard test suite
  my $TEST_SUITE = "summary";
  # Short test suites
#  my $TEST_SUITE = "summary-no-space";
#  my $TEST_SUITE = "test print_tokens";
  print_log("Dfec system tests...");

  my $command = "make $J2 -C $INV/tests/dfec-tests $TEST_SUITE " .
    "&> dfec_system_test.out";
  `$command`;
  if ($CHILD_ERROR) {
    print_log("FAILED\n");
    return 0;
  }

  $command = "make -C $INV/tests/dfec-tests summary-only " .
    "2>&1 | tee dfec_system_test_summary.out";
  my $result = `$command`;
  if ($CHILD_ERROR) {
    print_log("FAILED\n");
    return 0;
  }

  foreach my $line (split /\n/,$result) {
    next if ($line =~ /^make/);
    if (!($line =~ /^OK\s/)) {
      print_log("FAILED\n");
      return 0;
    }
  }

  print_log("OK\n");
  return 1;
}


# Appends its arguments to the log file.  If the quiet option was *not*
# specified, also prints its arguments to STDOUT.
sub print_log {
  open LOG, ">>$LOG" or die "can't open $LOG: $!\n";
  print LOG @_;
  if (! $quiet) {
    print @_;
  }
  close LOG;
}


# Source the file specified as an argument, and return the resulting
# environment in a hash
sub get_env {
  my ($file) = @_;
  my %newenv = ();
  my $newenv = `source $file; env`;
  if ($CHILD_ERROR) {
    print_log("FAILED: source $file; env\n");
    return 0;
  }
  foreach my $line (split '\n', $newenv) {
    my ($var, $val) = split '=', $line;
    $newenv{$var} = $val;
  }
  return %newenv;
}
