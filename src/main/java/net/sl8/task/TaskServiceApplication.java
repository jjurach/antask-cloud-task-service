package net.sl8.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.stream.Stream;

@EnableBinding(Sink.class)
@EnableDiscoveryClient
@SpringBootApplication
public class TaskServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskServiceApplication.class, args);
    }
}

@RestController
@RefreshScope
class MessageRestController {

    @Value("${message}")
    private String msg;

    @RequestMapping("/message")
    String read() {
        return this.msg;
    }

}

@MessageEndpoint
class TaskProcessor {

    private final TaskRepository taskRepository;

    @ServiceActivator(inputChannel = "input")
    public void acceptNewTasks(String tn) {
        this.taskRepository.save(new Task(tn));
    }

    @Autowired
    public TaskProcessor(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
}

@Component
class DummyCLR implements CommandLineRunner {

    private final TaskRepository taskRepository;

    @Autowired
    public DummyCLR(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Stream.of("this", "that", "the-other")
                .forEach(n -> taskRepository.save(new Task(n)));
        taskRepository.findAll().forEach(System.out::println);
    }
}

@RepositoryRestResource
interface TaskRepository extends JpaRepository<Task, Long> {
}

@Entity
class Task {

    @Id
    @GeneratedValue
    private Long id;

    private String taskName;

    Task() {
    }

    public Task(String taskName) {
        this.taskName = taskName;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                '}';
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Long getId() {
        return id;
    }

    public String getTaskName() {
        return taskName;
    }
}