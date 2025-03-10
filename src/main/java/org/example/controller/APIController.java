package org.example.controller;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.model.APIParam;
import org.example.model.ModelType;
import org.example.service.APIService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

@WebServlet(name = "APIServlet", value = "/api")
public class APIController extends HttpServlet {
    // 의존성 주입, 싱글턴 패턴을 기반으로 한
    final APIService apiService = APIService.getInstance();;
    final Logger logger = Logger.getLogger(APIController.class.getName());

    @Override
    public void init() throws ServletException {
        logger.info("EPL Team Recommender API Controller init...");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // Extract prompt and model from request parameters
        String prompt = req.getParameter("prompt");
        String modelParam = req.getParameter("model");

        // Validate parameters to prevent errors
        if (prompt == null || prompt.trim().isEmpty()) {
            handleErrorResponse(resp, "Prompt cannot be empty");
            return;
        }

        if (modelParam == null || modelParam.trim().isEmpty()) {
            handleErrorResponse(resp, "Model parameter cannot be empty");
            return;
        }

        try {
            ModelType model = ModelType.valueOf(modelParam);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            PrintWriter out = resp.getWriter();
            APIParam apiParam = new APIParam(prompt, model);

            try {
                out.println(apiService.callAPI(apiParam));
            } catch (Exception e) {
                logger.severe("Error calling API: " + e.getMessage());
                handleErrorResponse(resp, "Error processing your request: " + e.getMessage());
            }
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid model type: " + modelParam);
            handleErrorResponse(resp, "Invalid model type specified");
        }
    }

    private void handleErrorResponse(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("{\"error\": \"" + message + "\"}");
    }
}