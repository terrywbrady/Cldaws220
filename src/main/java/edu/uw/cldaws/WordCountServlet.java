package edu.uw.cldaws;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

public class WordCountServlet extends HttpServlet {

 public void doGet(HttpServletRequest request, 
  HttpServletResponse response) 
  throws ServletException, IOException {
  
  response.setContentType("application/json");
  WordCount wc = new WordCount();
  String url = request.getParameter("url");
  if (url == null) {
   url = "";
  }
  String res = wc.checkUrl(url);
  PrintWriter out = response.getWriter();
  out.println(res);
 }
}