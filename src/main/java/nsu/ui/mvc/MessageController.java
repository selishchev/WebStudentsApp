/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package nsu.ui.mvc;

import javax.validation.Valid;

import nsu.ui.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import nsu.ui.MessageRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Rob Winch
 */
@Controller
@RequestMapping("/")
public class MessageController {
	private final MessageRepository messageRepository;

	@Autowired
	public MessageController(MessageRepository messageRepository) {
		this.messageRepository = messageRepository;
	}

	@RequestMapping
	public ModelAndView list() {
		Iterable<Student> students = this.messageRepository.findAll();
		return new ModelAndView("messages/list", "students", students);
	}

	@RequestMapping("{id}")
	public ModelAndView view(@PathVariable("id") Student student) {
		if (student.getId() == null) {
			return new ModelAndView("messages/not_found", "student", student);
		}
		return new ModelAndView("messages/view", "student", student);
	}

	@RequestMapping(params = "form", method = RequestMethod.GET)
	public String createForm(@ModelAttribute Student student) {
		return "messages/form";
	}

	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView create(@Valid Student student, BindingResult result,
							   RedirectAttributes redirect) throws SQLException {
		if (result.hasErrors()) {
			return new ModelAndView("messages/form", "formErrors", result.getAllErrors());
		}
		student = this.messageRepository.save(student);
		redirect.addFlashAttribute("globalMessage", "Successfully created a new student");
		return new ModelAndView("redirect:/{student.id}", "student.id", student.getId()); //поменять на list
	}

	@RequestMapping("foo")
	public String foo() {
		throw new RuntimeException("Expected exception in controller");
	}

	@RequestMapping(params = "groups", method = RequestMethod.GET)
	public ModelAndView createGroups() throws SQLException {
		HashMap<String, ArrayList<Student>> students = this.messageRepository.findGroups();
		return new ModelAndView("messages/groups", "students", students);
	}

	@RequestMapping("{id}/form2")
	public ModelAndView editForm(@ModelAttribute Student student) {
		student = messageRepository.findStudent(student.getId());
		messageRepository.saveId(student);
		return new ModelAndView("messages/form2", "student", student);
	}

	@RequestMapping(params = "form2", method = RequestMethod.POST)
	public ModelAndView edit(@Valid Student student, BindingResult result,
							 RedirectAttributes redirect) throws SQLException {
		if (result.hasErrors()) {
			return new ModelAndView("messages/form2", "formErrors", result.getAllErrors());
	}
		student.setId(messageRepository.getId());
		student = messageRepository.update(student);
		redirect.addFlashAttribute("globalMessage", "Successfully edited the student");
		return new ModelAndView("redirect:/{student.id}", "student.id", student.getId());
}
}
