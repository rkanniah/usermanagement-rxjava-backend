package com.rk.usermanagement.controller;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rk.usermanagement.controller.response.UserOperationResponseStatus;
import com.rk.usermanagement.model.User;
import com.rk.usermanagement.service.UserService;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

@RestController
public class UserController {

	private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());

	private static final long THREAD_SLEEP_TIME = 3000; // sleep for 3 seconds

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public ResponseEntity<?> addUser(@RequestBody User user) {

		LOGGER.info("Adding a user..." + user);

		if (user == null || (user != null && CollectionUtils.isEmpty(user.getRole()))) {
			return new ResponseEntity<>("user or role cannot be empty or null!", HttpStatus.BAD_REQUEST);
		}

		final UserOperationResponseStatus addUserResponse = new UserOperationResponseStatus();

		Single.fromFuture(userService.addUser(user)).subscribeOn(Schedulers.io()).subscribe(
				onSuccess -> addUserResponse.setResponseEntity(new ResponseEntity<>(onSuccess, HttpStatus.CREATED)),
				onError -> addUserResponse
						.setResponseEntity(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(onError)));

		try {
			Thread.sleep(THREAD_SLEEP_TIME);
		} catch (InterruptedException e) {
		}

		return addUserResponse.getResponseEntity();
	}

	@RequestMapping(value = "/update", method = RequestMethod.PUT)
	public ResponseEntity<?> updateUser(@RequestBody User user) {

		LOGGER.info("Updating user...");

		if (user == null || (user != null && CollectionUtils.isEmpty(user.getRole()))) {
			return new ResponseEntity<>("user or role cannot be empty or null for update!", HttpStatus.BAD_REQUEST);
		}

		final UserOperationResponseStatus updateUserResponse = new UserOperationResponseStatus();

		Single.fromFuture(userService.updateUser(user)).subscribeOn(Schedulers.io()).subscribe(
				onSuccess -> updateUserResponse.setResponseEntity(new ResponseEntity<>(onSuccess, HttpStatus.ACCEPTED)),
				onError -> {

					if (onError instanceof DataAccessException) {

						updateUserResponse.setResponseEntity(
								ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(onError));
					} else {
						updateUserResponse.setResponseEntity(
								new ResponseEntity<>("User not found for update: " + user, HttpStatus.BAD_REQUEST));
					}

				});

		try {
			Thread.sleep(THREAD_SLEEP_TIME);
		} catch (InterruptedException e) {
		}

		return updateUserResponse.getResponseEntity();

	}

	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteUser(@RequestBody User user) {

		LOGGER.info("Deleting user..." + user);

		final UserOperationResponseStatus deleteUserResponse = new UserOperationResponseStatus();

		Completable.create(emitter -> {
			userService.deleteUser(user);
			emitter.onComplete();
		}).andThen(Observable.just(1)).subscribeOn(Schedulers.io()).subscribe(
				onNext -> deleteUserResponse.setResponseEntity(new ResponseEntity<>(null, HttpStatus.ACCEPTED)),
				onError -> deleteUserResponse
						.setResponseEntity(new ResponseEntity<>(onError, HttpStatus.INTERNAL_SERVER_ERROR)));

		try {
			Thread.sleep(THREAD_SLEEP_TIME);
		} catch (InterruptedException e) {
		}

		LOGGER.info("FINAL response: " + deleteUserResponse.getResponseEntity());

		return deleteUserResponse.getResponseEntity();

	}

	@RequestMapping(value = "/displayAll", method = RequestMethod.GET)
	public ResponseEntity<?> getAllUsers() {

		LOGGER.info("Retrieving all users...");

		final UserOperationResponseStatus getAllUsersResponse = new UserOperationResponseStatus();

		Single.fromFuture(userService.getAllUsers()).subscribeOn(Schedulers.io()).subscribe(onSuccess -> {

			if (CollectionUtils.isEmpty(onSuccess)) {
				getAllUsersResponse.setResponseEntity(new ResponseEntity<>(onSuccess, HttpStatus.NOT_FOUND));
			} else {
				getAllUsersResponse.setResponseEntity(new ResponseEntity<>(onSuccess, HttpStatus.OK));
			}

		}, onError -> getAllUsersResponse
				.setResponseEntity(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(onError)));

		try {
			Thread.sleep(THREAD_SLEEP_TIME);
		} catch (InterruptedException e) {
		}

		return getAllUsersResponse.getResponseEntity();
	}

	@RequestMapping(value = "/find", method = RequestMethod.GET)
	public ResponseEntity<?> findUser(@RequestParam(name = "email") String email) {

		LOGGER.info("Retrieving a single user...");

		if (email == null || email.trim().isEmpty()) {
			return new ResponseEntity<>("User not found with email: " + email, HttpStatus.NOT_FOUND);
		}

		final UserOperationResponseStatus findUserResponse = new UserOperationResponseStatus();

		Single.fromFuture(userService.findUser(email)).subscribeOn(Schedulers.io()).subscribe(onSuccess -> {

			if (onSuccess == null || (onSuccess != null && onSuccess.getId() == null)) {
				findUserResponse.setResponseEntity(new ResponseEntity<>(onSuccess, HttpStatus.NOT_FOUND));
			} else {
				findUserResponse.setResponseEntity(new ResponseEntity<>(onSuccess, HttpStatus.OK));
			}

		}, onError -> findUserResponse
				.setResponseEntity(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(onError)));

		try {
			Thread.sleep(THREAD_SLEEP_TIME);
		} catch (InterruptedException e) {
		}

		return findUserResponse.getResponseEntity();
	}
}