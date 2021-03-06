package biz.deinum.multitenant.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import biz.deinum.multitenant.core.ContextHolder;
import biz.deinum.multitenant.web.ContextRepository;

/**
 * {@code javax.servlet.Filter} which sets the context from the current request.
 * Delegates the actual lookup to a {@code ContextRepository}.
 * 
 * When no context is found an IllegalStateException is thrown, this can be
 * switched of by setting the <code>throwExceptionOnMissingContext</code>
 * property.
 * 
 * @author Marten Deinum
 * @since 1.3
 * @see biz.deinum.multitenant.web.servlet.ContextInterceptor
 */
public class ContextFilter extends OncePerRequestFilter {

	private final Logger logger = LoggerFactory.getLogger(ContextFilter.class);

	private final ContextRepository contextRepository;

	private boolean throwExceptionOnMissingContext = true;

	public ContextFilter(ContextRepository contextRepository) {
		super();
		this.contextRepository = contextRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			String context = contextRepository.getContext(request, response);
			logger.debug("Using context: {}", context);
			if (throwExceptionOnMissingContext && !StringUtils.hasText(context)) {
				throw new IllegalStateException(
						"Could not determine context for current request!");
			} else {
				ContextHolder.setContext(context);
				filterChain.doFilter(request, response);
			}
		} finally {
			// Always clear the thread local after request processing.
			ContextHolder.clear();
		}
	}

	/**
	 * When <code>true</code> (the default) an exception is throw if no context is
	 * found for the current request.
	 * 
	 * @param throwExceptionOnMissingContext
	 */
	public void setThrowExceptionOnMissingContext(
			boolean throwExceptionOnMissingContext) {
		this.throwExceptionOnMissingContext = throwExceptionOnMissingContext;
	}
}
