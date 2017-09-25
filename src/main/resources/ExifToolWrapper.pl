#!/usr/bin/perl

#-------------------------------------------------------------------------------
# Imports

# Safe mode
use strict;
use warnings;

# Add current directory to library path
use lib ".";

# Import encode/decode functions
use Protocol;

# Import ExifTool library
use Image::ExifTool qw(:Public);

#-------------------------------------------------------------------------------

# Create the ExifTool instance
my $tool = new Image::ExifTool;

# List of tags to query for
my $tagList = undef;

# Repeatedly read requests from STDIN and send responses to STDOUT...
while (readBegin())
{
    # Read the first byte, which is the request type
    my $type = readByte();

    # Read/handle the rest of the request, based on type
    if ($type == REQUEST_SETOPTION)
    {
        setOption();
    }
    elsif ($type == REQUEST_CLEAROPTIONS)
    {
        clearOptions();
    }
    elsif ($type == REQUEST_SETTAGS)
    {
        setTags();
    }
    elsif ($type == REQUEST_EXTRACTINFO)
    {
        extractInfo();
    }
    elsif ($type == REQUEST_TEST)
    {
        test();
    }
    else
    {
        die("Unknown request type: $type");
    }
}

exit 0;

#-------------------------------------------------------------------------------
# Helper functions:

sub setOption
{
    # Read the rest of the request
    my $name = readString();
    my $value = readString();
    readEnd();

    # Perform action
    $tool->Options($name => $value);

    # Send response
    sendOK();
}

sub clearOptions
{
    # Read the rest of the request
    readEnd();

    # Perform action
    $tool->ClearOptions();

    # Send response
    sendOK();
}

sub setTags
{
    # Read the rest of the request
    my $count = readInt();
    my @tags = ();
    for (my $i = 0; $i < $count; $i++)
    {
        push(@tags, readString());
    }
    readEnd();

    # Store list of tag names to be requested
    $tagList = ($count == 0) ? undef : \@tags;

    # Send response
    sendOK();
}

sub extractInfo
{
    # Read the rest of the request
    my $filename = readString();
    readEnd();

    # Perform action
    my $result = defined $tagList ? $tool->ExtractInfo($filename, $tagList) : $tool->ExtractInfo($filename);

    # Check result, then send response
    if ($result == 1)
    {
        # Get the desired tag values into a hash
        my $info = defined $tagList ? $tool->GetInfo($tagList) : $tool->GetInfo();
        sendTagInfo(1, $info);
    }
    else
    {
        sendTagInfo(2, undef);
    }
}

sub test
{
    my $byte = readByte();
    my $int = readInt();
    my $string = readString();
    my $binary = readBinary();
    readEnd();

    sendEcho($byte, $int, $string, $binary);
}



