package cn.krossframework.web;

import cn.krossframework.state.ExecuteTask;
import cn.krossframework.state.WorkerManager;
import cn.krossframework.web.cat.CatTask;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication @RestController public class WebApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebApplication.class, args);
	}

	private final WorkerManager workerManager;

	public WebApplication(WorkerManager workerManager) {
		this.workerManager = workerManager;
	}

	@RequestMapping({ "/addCat/{id}", "/addCat" })
	public void addCat(@PathVariable(value = "id", required = false) Long id) {
		this.workerManager.enter(new ExecuteTask(id, new CatTask(0), null));
	}

	@RequestMapping("/killCat/{id}") public void killCat(@PathVariable("id") Long id) {
		this.workerManager.addTask(new ExecuteTask(id, new CatTask(1), () -> {
			System.out.println("kill cat fail:" + id);
		}));
	}
}
