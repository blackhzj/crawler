package cn.xiaoyanol.crawler;

import cn.xiaoyanol.crawler.domain.baseinfo.BaseInfoData;
import cn.xiaoyanol.crawler.domain.baseinfo.BaseInfoJsonRootBean;
import cn.xiaoyanol.crawler.domain.baseinfo.BaseInfoLegalInfo;
import cn.xiaoyanol.crawler.domain.search.SearchCompanyList;
import cn.xiaoyanol.crawler.domain.search.SearchJsonRootBean;
import cn.xiaoyanol.crawler.service.IBaseInfoService;
import cn.xiaoyanol.crawler.service.ISearchService;
import cn.xiaoyanol.crawler.service.impl.BaseInfoServiceImpl;
import cn.xiaoyanol.crawler.service.impl.SearchServiceImpl;
import cn.xiaoyanol.crawler.utils.ExcelExportUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 *
 * @Author: chenyanfeng
 * @Date: 2018-11-25
 * @Time: 下午9:39
 */
public class Main {
    public static void main(String[] args) throws Exception {

        ISearchService searchService = new SearchServiceImpl();
        IBaseInfoService baseInfoService = new BaseInfoServiceImpl();

//        SearchJsonRootBean searchResult = searchService.getSearchResult("阿里巴巴");
//        Gson searchJson = new GsonBuilder().setPrettyPrinting().create();
//        System.out.println(searchJson.toJson(searchResult));

        //要获取信息的公司名单
        File companyFile = new File("company2.txt");
//        File companyFile = new File("company3.txt");
        FileReader fileReader = new FileReader(companyFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> companyNameList = new ArrayList<String>();
        String companyName = null;
        while ((companyName = bufferedReader.readLine()) != null) {
            companyNameList.add(companyName.trim().replaceAll("　", ""));
        }
        bufferedReader.close();
        fileReader.close();

        List<String> notExistCompanyList = new ArrayList<String>();
        List<List<Object>> excelData= new ArrayList<List<Object>>();
        Gson baseInfoJson = new GsonBuilder().setPrettyPrinting().create();

        int i = 0;
        for (String company_name : companyNameList) {
            if (StringUtils.isEmpty(company_name))
            {
                continue;
            }
            System.out.println("查询公司：" + company_name);
            i++;
//            if (i % 10 == 0)
//            {
//                Thread.sleep(10000);
//            }
            SearchJsonRootBean searchResult = searchService.getSearchResult(company_name);
            List<SearchCompanyList> companyList = searchResult.getData().getCompanyList();
            if (companyList.size() == 0)
            {
                notExistCompanyList.add(company_name);
//                throw new Exception("数据不存在！ 公司：" + company_name);
                continue;
            }
            System.out.println("查询到的公司列表：");
            for (SearchCompanyList searchCompanyList : companyList) {
                System.out.println(searchCompanyList.getName());
            }
            //            for (SearchCompanyList searchCompanyList : subList) {
//
//            }
//            List<SearchCompanyList> subList = companyList.subList(0, 1);

            SearchCompanyList searchCompanyList = companyList.get(0);
            long companyId = searchCompanyList.getId();
            BaseInfoJsonRootBean baseInfoResult = baseInfoService.getBaseInfoResult(String.valueOf(companyId));
//            System.out.println(baseInfoJson.toJson(baseInfoResult));
            BaseInfoData baseInfoData = baseInfoResult.getData();
            List<Object> dataList = new ArrayList<Object>();
            System.out.println("id: " + baseInfoData.getId() + " name: " + baseInfoData.getName());
            dataList.add(company_name);
            dataList.add(baseInfoData.getId());
            dataList.add(baseInfoData.getName());
            BaseInfoLegalInfo legalInfo = baseInfoData.getLegalInfo();
            if (legalInfo == null) {
//                System.out.println(baseInfoJson.toJson(baseInfoResult));
            } else {
                String legalInfoName = legalInfo.getName();
                System.out.println("legalInfoName: " + legalInfoName);
                dataList.add(legalInfoName);
            }
            String regStatus = baseInfoData.getRegStatus();
            System.out.println("regStatus: " + regStatus);
            dataList.add(regStatus);
            excelData.add(dataList);

        }
//        BaseInfoJsonRootBean baseInfoResult = baseInfoService.getBaseInfoResult("1698375");
//        BaseInfoJsonRootBean baseInfoResult = baseInfoService.getBaseInfoResult("2316127550");
//        Gson baseInoJson = new GsonBuilder().setPrettyPrinting().create();
//        System.out.println(baseInoJson.toJson(baseInfoResult));


        System.out.println("不存在的公司列表：" + notExistCompanyList);




        List<String> rowNames = new ArrayList<String>();
        rowNames.add("查询公司名");
        rowNames.add("id");
        rowNames.add("公司名");
        rowNames.add("法人信息");
        rowNames.add("经营状态");
        ExcelExportUtils excelExportUtils = new ExcelExportUtils(rowNames, excelData);
        excelExportUtils.exportData();
    }
}
