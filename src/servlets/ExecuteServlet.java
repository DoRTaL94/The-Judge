package servlets;

import com.google.gson.Gson;
import exceptions.RequestBodyFieldsException;
import models.Response;
import services.CodeService;
import services.ServletsService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/execute")
public class ExecuteServlet extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        CodeService codeService = ServletsService.getCodeService(request.getServletContext());
        Gson gson = new Gson();

        try {
            CodeService.ExecutionCallable executionCallable = codeService.compileAndRunCode(request);
            Response res;

            if(executionCallable == null) {
                res = new Response("Programming language not supported");
            } else {
                res = executionCallable.call();
            }

            out.print(gson.toJson(res));
            out.flush();
        } catch(RequestBodyFieldsException e) {
          out.print(String.format("{\"message\": \"%s\"}", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
