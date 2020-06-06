import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class Crawl {
	private static WebDriver driver;
	public static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
	public static final String WEB_DRIVER_PATH = "D:/Program Files/selenium/chromedriver.exe";
	private static BufferedWriter bufferWriter;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// 게시판 별로 crawl
		String url_academic = "https://computer.cnu.ac.kr/computer/notice/bachelor.do";
		String url_general = "https://computer.cnu.ac.kr/computer/notice/notice.do";
		String url_business = "https://computer.cnu.ac.kr/computer/notice/project.do";
		String url_job = "https://computer.cnu.ac.kr/computer/notice/job.do";
		String url_news = "https://computer.cnu.ac.kr/computer/notice/cse.do";

		Crawl c = new Crawl();
		c.crawl(url_academic, "academic");
		c.crawl(url_general, "general");
		c.crawl(url_business, "business");
		c.crawl(url_job, "job");
		c.crawl(url_news, "news");
		
		driver.close();
	}

	// 생성자
	public Crawl() {
		super();

		System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);
		ChromeOptions options = new ChromeOptions();
		options.addArguments("headless");
		driver = new ChromeDriver(options);
	}

	private void crawl(String url, String fileName) {
		System.out.println("<" + fileName + ">");
		List<String> list = new ArrayList<>();
		try {
			driver.get(url);

			Boolean prevPage_check = driver.findElements(By.xpath("//li[@class='prev pager']/a")).size() > 0;
			Boolean nextPage_check = driver.findElements(By.xpath("//li[@class='next pager']/a")).size() > 0;
			int page = 1;

			if (prevPage_check == false) { // 1페이지면

				List<WebElement> check_top = driver
						.findElements(By.xpath("//div[@class='b-title-box']/parent::*/parent::*"));
				List<WebElement> title = driver.findElements(By.xpath("//div[@class='b-title-box']/a"));
				List<WebElement> date = driver.findElements(By.xpath("//span[@class='b-date']"));
				List<WebElement> see = driver.findElements(By.xpath("//span[@class='hit']"));

				// 고정 공지 저장
				for (int i = 0; i < title.size(); i++) {

					if (check_top.get(i).getAttribute("class").toString().trim().equals("b-top-box")) {
						String titledata = title.get(i).getText().trim();
						titledata = titledata.replaceAll(",", " ");
						list.add(titledata);
						list.add(date.get(i).getAttribute("innerHTML").trim());
						String seedata = see.get(i).getAttribute("innerHTML").trim();
						String[] splited = seedata.split(" ");
						list.add(splited[1]);
					}
				}

				while (nextPage_check) { // 마지막 페이지 전까지 크롤링
					// 일반 공지 저장
					for (int i = 0; i < title.size(); i++) {

						if (!check_top.get(i).getAttribute("class").toString().trim().equals("b-top-box")) {
							String titledata = title.get(i).getText().trim();
							titledata = titledata.replaceAll(",", " ");
							list.add(titledata);
							list.add(date.get(i).getAttribute("innerHTML").trim());
							String seedata = see.get(i).getAttribute("innerHTML").trim();
							String[] splited = seedata.split(" ");
							list.add(splited[1]);
						}
					}

					// 다음 페이지로 이동
					driver.findElement(By.xpath("//li[@class='next pager']/a")).click();
					nextPage_check = driver.findElements(By.xpath("//li[@class='next pager']/a")).size() > 0;
					check_top = driver.findElements(By.xpath("//div[@class='b-title-box']/parent::*/parent::*"));
					title = driver.findElements(By.xpath("//div[@class='b-title-box']/a"));
					date = driver.findElements(By.xpath("//span[@class='b-date']"));
					see = driver.findElements(By.xpath("//span[@class='hit']"));
					System.out.println(page + " 페이지 완료");
					page++;
				}
				
				// 마지막 페이지 저장
				for (int i = 0; i < title.size(); i++) {
					if (!check_top.get(i).getAttribute("class").toString().trim().equals("b-top-box")) {
						String titledata = title.get(i).getText().trim();
						titledata = titledata.replaceAll(",", " ");
						list.add(titledata);
						list.add(date.get(i).getAttribute("innerHTML").trim());
						String seedata = see.get(i).getAttribute("innerHTML").trim();
						String[] splited = seedata.split(" ");
						list.add(splited[1]);
					}
				}
				System.out.println(page + " 페이지 완료(마지막)");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 저장
		saveCsv(list, fileName);
	}

	// csv 파일 생성 : 제목 | 날짜 | 조회수
	private static void saveCsv(List<String> list, String fileName) {
		try {
			FileOutputStream fos = new FileOutputStream(fileName + ".csv");
			OutputStreamWriter osw = new OutputStreamWriter(fos, "EUC-KR");
			bufferWriter = new BufferedWriter(osw);
			StringBuilder sb = new StringBuilder();
			for (int i = 0, cnt = 1; i < list.size(); i++, cnt++) {
				sb.append(list.get(i) + ",");
				if (cnt % 3 == 0) {
					sb.append("\n");
					cnt = 0;
				}
			}
			bufferWriter.write(sb.toString());
			bufferWriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			try {
				bufferWriter.close();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			System.out.println(fileName + " 파일 생성");
		}
	}
}
