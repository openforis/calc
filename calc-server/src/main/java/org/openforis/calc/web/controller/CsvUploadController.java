package org.openforis.calc.web.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.openforis.commons.io.csv.CsvReader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * Csv Uploader
 * 
 * @author M. Togna
 * 
 */
@Controller
@RequestMapping(value = "/rest")
public class CsvUploadController {

	private SimpleDateFormat fileNameFormat = new SimpleDateFormat("'file'-yyyyMMdd-hhmmss.SSS");

	@RequestMapping(value = "/csv-upload.json", method = RequestMethod.POST, produces = "application/json")
	public synchronized @ResponseBody
	Response upload(@ModelAttribute("file") MultipartFile file) {
		try {
			// upload file
			File tempFile = File.createTempFile(fileNameFormat.format(new Date()), ".csv");
			FileUtils.copyInputStreamToFile(file.getInputStream(), tempFile);

			@SuppressWarnings( "resource" )
			CsvReader csvReader = new CsvReader(tempFile.getAbsolutePath());
			csvReader.readHeaders();

			Response response = new Response();
			response.addField("filepath", tempFile.getAbsolutePath());
			response.addField("headers", csvReader.getColumnNames());

			return response;
		} catch (Exception e) {
			throw new RuntimeException("Error while uploading file", e);
		}
	}
}
