package net.h2demo;

import javax.servlet.http.PushBuilder;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
	@GetMapping("/")
	String home() {
		return "index";
	}

	@GetMapping("/push-test")
	String push(PushBuilder pushBuilder) {
		pushBuilder
				.path("/h2-1.jpg")
				.addHeader("content-type", "image/png")
				.push();

		pushBuilder
				.path("/h2-2.jpg")
				.addHeader("content-type", "image/png")
				.push();

		return "index";
	}

}
