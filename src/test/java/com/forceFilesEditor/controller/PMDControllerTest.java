package com.forceFilesEditor.controller;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class PMDControllerTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void getMethodSuggestion() throws ParseException {
        Date date = new Date("Mon May 07 21:54:09 UTC 2018");
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        String dateString = formatter.format(date);
        DateUtils.parseDateStrictly(dateString, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    }
}