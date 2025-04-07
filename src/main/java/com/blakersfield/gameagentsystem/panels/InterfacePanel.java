package com.blakersfield.gameagentsystem.panels;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.*;

import org.apache.http.impl.client.CloseableHttpClient;

import com.blakersfield.gameagentsystem.llm.clients.SqlLiteDao;

import java.awt.*;
import java.sql.*;

public class InterfacePanel extends ChatPanel{
    public InterfacePanel(CloseableHttpClient httpClient, SqlLiteDao sqlLiteDao, String apiUrl){
        super(httpClient, sqlLiteDao, apiUrl);
    }
}
