package com.yuaiwan.utils;
 
import java.util.List;

/**
 * 分页的处理类 
 * @author guangyu
 */
@SuppressWarnings("serial")
public class Pagination<T> extends BasePagination<T> {
	private final String jumpFunction = "queryPage";
	
	public Pagination() {
		super();
	}
	
	public Pagination(int pageNum, int pageSize, int totalCount, List<T> list) {
		super(pageNum, pageSize, totalCount, list);
	}
	
	@Override
    public String getPages(){
    	StringBuffer sb = new StringBuffer();
    	sb.append("<ul class='pagination fr'>");
    	if(totalCount>0){
			sb.append("<li class='total_page'><a>共" + totalCount + "条记录</a></li>");
    	}
		if (totalPage > 1) {//判断只有一页记录
			if (pageNum == totalPage) {// 判断是否是最后一页
				sb.append("<li><a href=\"javascript:void(0)\" onclick=\""+ jumpFunction + "(" + getPreviousPage()+ ");\" class='pre'>上一页</a></li>");
				pageFenye(sb);
			}else {
				if (pageNum > 1) {
					sb.append("<li><a href=\"javascript:void(0)\" onclick=\""+ jumpFunction + "(" + getPreviousPage()+ ");\" class='pre'>上一页</a></li>");
				}
				pageFenye(sb);
				sb.append("<li><a href=\"javascript:void(0)\" onclick=\""+ jumpFunction + "(" + getNextPage()+ ");\" class='next'>下一页</a></li>");
			}
			if(sb.toString().contains("…")){
				sb.append("<li  class='last_page'><a href='javascript:void(0);'><input style='padding: 0px 0px;' type='text' size='1' onchange='if(+this.value >= 1 && +this.value <= "+totalPage+") "+jumpFunction+"(+this.value)' />&nbsp;跳转</a></li>");
			}
		}
    	sb.append("</ul>");
    	return sb.toString();
    }
    
    // 1 2 3 4 5 6 7 
    // 1 . 4 5 6 . 9
    private void pageFenye(StringBuffer sb){
		if (totalPage > pageShow) {
			int startPage = pageNum - halfPageShow;
			int endPage = pageNum + halfPageShow;
			if(startPage<=1){
				startPage = 1;
				endPage = startPage+halfPageShow+halfPageShow;
				if(endPage>totalPage){
					endPage = totalPage;
				}
			}
			if(endPage>=totalPage){
				endPage = totalPage;
				startPage = endPage-halfPageShow-halfPageShow;
				if(startPage<1){
					startPage = 1;
				}
			}
			if(startPage <= 1){//(<=1: ;==2:1 ,==3:1 2 ,>3:1... )
				startPage = 1;
				endPage = endPage + 2;
				if(endPage>totalPage){
					endPage = totalPage;
				}
			}else if(startPage == 2){
				endPage = endPage + 1;
				if(endPage>totalPage){
					endPage = totalPage;
				}
			}
			if(endPage>=totalPage){//(>=totalPage: ;==totalPage-1:totalPage)
				endPage = totalPage;
				startPage = startPage - 2;
				if(startPage<1){
					startPage = 1;
				}
			}else if(endPage==(totalPage-1)){
				startPage = startPage - 1;
				if(startPage<1){
					startPage = 1;
				}
			}
			if(startPage <= 1){//(<=1: ;==2:1 ,==3:1 2 ,>3:1... )
				startPage = 1;
			}else if(startPage == 2){
				sb.append("<li><a href=\"javascript:void(0)\" onclick=\""+ jumpFunction + "(1);\">1</a></li>");
			}else if(startPage == 3){
				sb.append("<li><a href=\"javascript:void(0)\" onclick=\""+ jumpFunction + "(1);\">1</a></li>");
				sb.append("<li><a href=\"javascript:void(0)\" onclick=\""+ jumpFunction + "(2);\">2</a></li>");
			}else{
				sb.append("<li><a href=\"javascript:void(0)\" onclick=\""+ jumpFunction + "(1);\">1</a></li>");
				sb.append("<li><a class='disabled'>…</a></li>");
			}
			for (int i = startPage; i <= endPage ; i++) {// 判断页码,如果是当前页
				if (pageNum == i) {
					sb.append("<li class='active'><a>" + i + "</a></li>");
				} else {
					sb.append("<li><a href=\"javascript:void(0)\" onclick=\""+ jumpFunction + "(" + i + ");\">" + i + "</a></li>");
				}
			}
			if(endPage>=totalPage){//(>=totalPage: ;==totalPage-1:totalPage)
				endPage = totalPage;
			}else if(endPage==(totalPage-1)){
				sb.append("<li><a href=\"javascript:void(0)\" onclick=\""+ jumpFunction + "("+totalPage+");\">"+totalPage+"</a></li>");
			}else if(endPage==(totalPage-2)){
				sb.append("<li><a href=\"javascript:void(0)\" onclick=\""+ jumpFunction + "("+(totalPage-1)+");\">"+(totalPage-1)+"</a></li>");
				sb.append("<li><a href=\"javascript:void(0)\" onclick=\""+ jumpFunction + "("+totalPage+");\">"+totalPage+"</a></li>");
			}else{
				sb.append("<li><a class='disabled'>…</a></li>");
				sb.append("<li><a href=\"javascript:void(0)\" onclick=\""+ jumpFunction + "("+totalPage+");\">"+totalPage+"</a></li>");
			}
		}else {// 如果小于pageShow页
			for (int i = 1; i <= totalPage; i++) {
				if (pageNum == i) {// 判断页码
					sb.append("<li class='active'><a>" + i + "</a></li>");
				} else {
					sb.append("<li><a href=\"javascript:void(0)\" onclick=\""+ jumpFunction + "(" + i + ");\">" + i + "</a></li>");
				}
			}
		}
    }
}
