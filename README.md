# The-Judge

## Overview

The Judge is a Restful API code judge written in Java and runs on Apache Tomcat 8 server.  
The Judge is currently supporting Java code and will support more languages in the near future.  
The Judge executes code inside a sandbox to avoid malicious code execution.  

## Download

The most updated version of The Judge can be downloaded [here](https://drive.google.com/open?id=1GDjWZ-Htvem_ecNWZvgNfed7Z53ZlHjY).  
Extract the zip file and run `TheJudge-startup.bat`.  
The Judge should be available on route `/TheJudge`.  

## API

The Judge has a single route for code execution.  
In order to execute code the client should send the following body to `/TheJudge/execute` :

```
{    
  "lang": "<Language in which the code is written with>",  
  "code": "<Code to be executed>",  
  "tests": "<Tests for code output>"  
}
```

The client will get back the response in the following json format:

```
{
  "message": "<Custom message of The Judge regarding the execution>",  
  "output": "<Raw output of the execution, empty if there are errors>",  
  "errors": "<Compilation or runtime errors>",  
  "durationInSeconds": "<Runtume duration time of the program, or -1 if there are errors>",  
  "testsFailed": "<Tests that failed separated by space>"  
}
```

### Example

Sending the following request body to The Judge:

```
{
  "lang": "Java",
  "code": "public class Solution {
      public static String sayHello() {
        System.out.println(\"This string will return in the 'output' field of the response\");
        return \"Hey\";
      }
    }",
  "tests": "public class Tests {
    @Test
    public void Test1() {
      assertEquals(\"Hello\", Solution.sayHello());
    }
    
    @Test
    public void Test2() {
      assertEquals(\"Sup\", Solution.sayHello());
    }
  }"
}
```

will yield the below response:

```
{
  "message": "Program executed successfully.
    Duration: 0.06 s.
    But the following test cases failed:
    Test1 Test2",
  "output": "This string will return in the 'output' field of the response",
  "errors": "",
  "durationInSeconds": 0.06,
  "testsFailed": "Test1 Test2"
}
```
