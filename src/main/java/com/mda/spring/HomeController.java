package com.mda.spring;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.mda.test.ProfileImage;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

/**
 * 
 * @author Venkat
 * 
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	MongoTemplate mongoTemplate;

	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! ");

		MongoOperations mongoOps = (MongoOperations) mongoTemplate;

		Set<String> collections = mongoOps.getCollectionNames();

		for (String s : collections) {
			logger.info("Collection name:" + s);
		}

		return "upload";
	}

	/**
	 * Upload single file using Spring Controller
	 */
	@RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
	public String uploadFileHandler(@RequestParam("name") String name, @RequestParam("file") MultipartFile file,
			Model model, HttpServletRequest request) {
		logger.info("entered into uploadFileHandler() method ");
		if (!file.isEmpty()) {
			try {

				byte[] bytes = file.getBytes();

				// Creating the directory to store file
				String rootPath = System.getProperty("catalina.home");
				logger.info("Root Path" + rootPath);
				// File dir = new File(rootPath + File.separator + "tmpFiles");
				File dir = new File(rootPath + File.separator + request.getContextPath() + File.separator + "resources"
						+ File.separator + "images");

				if (!dir.exists())
					dir.mkdirs();

				// Create the file path on server
				File serverFile = new File(dir.getAbsolutePath() + File.separator + file.getOriginalFilename());
				BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
				stream.write(bytes);
				stream.close();

				// Save file to MongoDB
				GridFS gfsPhoto = new GridFS(mongoTemplate.getDb(), "photo");
				GridFSInputFile gfsFile = gfsPhoto.createFile(serverFile);
				gfsFile.setFilename(file.getOriginalFilename());
				gfsFile.setContentType(file.getContentType());
				gfsFile.save();

				// prepare display message
				String message = "You have successfully uploaded file=" + name;
				model.addAttribute("message", message);

				return "uploaded";
			} catch (Exception e) {
				logger.debug("You failed to upload " + name + " => " + e.getMessage());
				return "upload";
			}
		} else {
			logger.debug("You failed to upload " + name + " because the file was empty.");
			return "upload";
		}
	}

	/**
	 * Download list of images from DB
	 */
	@RequestMapping(value = "/listImages", method = RequestMethod.GET)
	public String downloadFileHandler(Model model, HttpServletRequest request) {

		logger.info("Entered into downloadFileHandgler");

		List<ProfileImage> imageNames = new ArrayList<ProfileImage>();
		ProfileImage profile = null;
		String rootPath = System.getProperty("catalina.home");
		File dir = new File(rootPath + File.separator + request.getContextPath() + File.separator + "resources"
				+ File.separator + "images");
		
		// get saved images on DB
		GridFS gfsPhoto = new GridFS(mongoTemplate.getDb(), "photo");
		DBCursor cursor = gfsPhoto.getFileList();
		try {
			while (cursor.hasNext()) {
				DBObject image = cursor.next();
				
				profile = new ProfileImage();
				profile.setName(image.get("filename").toString());
				profile.setPath(dir.getAbsolutePath().toString() + File.separator + image.get("filename").toString());
				profile.setType(image.get("contentType").toString());

				// retrieve image from DB
				GridFSDBFile imageForOutput = gfsPhoto.findOne(profile.getName());
				imageForOutput.writeTo(profile.getPath());// output to new file

				imageNames.add(profile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		model.addAttribute("images", imageNames);

		return "imageslist";

	}

}
