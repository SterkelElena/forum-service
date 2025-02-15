package ait.cohort34.security.filter;

import ait.cohort34.accounting.model.Role;
import ait.cohort34.post.dao.PostRepository;
import ait.cohort34.post.model.Post;
import ait.cohort34.accounting.model.User;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Order(60)
public class DeletePostFilter implements Filter {

    final PostRepository postRepository;

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        if (checkEndPoint(request.getMethod(), request.getServletPath())) {
            User principal = (User) request.getUserPrincipal();
            String[] parts = request.getServletPath().split("/");
            String postId = parts[parts.length - 1];
            Post post = postRepository.findById(postId).orElse(null);
            if (post == null) {
                response.sendError(404, "Not found");
                return;
            }
            if (!(principal.getName().equals(post.getAuthor()) || principal.getRoles().contains(Role.MODERATOR.name()))) {
                response.sendError(403, "You do not have permission to access this resource");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private boolean checkEndPoint(String method, String path) {
        return HttpMethod.DELETE.matches(method) && path.matches("/forum/post/\\w+");
    }

}