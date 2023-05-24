package actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import constants.AttributeConst;
import constants.ForwardConst;
import constants.PropertyConst;

/**
 * 各ctionの親クラス。共通処理を行う。
 * @author User
 *
 */

public abstract class ActionBase {
    protected ServletContext context;
    protected HttpServletRequest request;
    protected HttpServletResponse response;

    /**
     * 初期化
     *サーブレットコンテキスト、リクエスト、レスポンスをクラスフィールドに設定
     *@param servletContext
     *@param servletRequest
     *@param servletResponse
     */

    public void init(
            ServletContext servletContext,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        this.context = servletContext;
        this.request = servletRequest;
        this.response = servletResponse;
    }

    /**
     * フロントコントローラから呼び出されるメソッド
     * @throws ServletException
     * @throws IOException
     */

    public abstract void process() throws ServletException, IOException;

    /**
     * パラメータのcommandの値に該当するメソッドを実行する
     * @throws ServletException
     * @throws ServletException
     */

    protected void invoke()
            throws ServletException, IOException{

        Method commandMethod;
        try {

            //パラメータからommandを取得
            String command = request.getParameter(ForwardConst.CMD.getValue());

            //commandにが移動するメソッドを実行する
            //例：action=Employee, command=show の時、EmployeeActionクラスのshowメソッドを実行
            /**
             * getMethod() は引数の型のリストを受け取ります。
             * 長さ 0 の配列を渡すということは、
             * 引数がないことを意味します。invoke() についても同様です。
             */

            commandMethod = this.getClass().getDeclaredMethod(command, new Class[0]);
            commandMethod.invoke(this, new Object[0]);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NullPointerException e) {

            //発生した例外をコンソールに表示
            e.printStackTrace();
            //commandの値が不正で実行できない場合はエラー画面を呼び出し
            forward(ForwardConst.FW_ERR_UNKNOWN);

        }
    }

    /**
     * 指定されたjspの呼び出しを行う
     * @param target 遷移先sp画面のファイル名(拡張子を含まない)
     * @throws ServletException
     * @throws IOException
     */

    protected void forward(ForwardConst target) throws ServletException, IOException {

        //jspファイルの相対パスを生成
        String forward = String.format("/WEB-INF/views/%s.jsp", target.getValue());
        RequestDispatcher dispatcher = request.getRequestDispatcher(forward);

        //jspファイルの呼び出し
        dispatcher.forward(request, response);
    }

    /**
     * URLを構築しリダイレクトを行う
     * @param action パラメータに設定する値
     * @param command パラメータに設定する値
     * @throws ServletException
     * @throws IOException
     */

    protected void redirect(ForwardConst action, ForwardConst command)
            throws ServletException, IOException {

        //URLを構築
        String redirectUrl = request.getContextPath() + "/?action=" + action.getValue();
        if (command != null) {
            redirectUrl = redirectUrl + "&command=" + command.getValue();
        }

        //URLへリダイレクト
        response.sendRedirect(redirectUrl);
    }

    /**
     * CSRF対策 token不正の場合はエラー画面を表示
     * @return true: token有効 false: token不正
     * @throws ServletExceptionx
     * @throws IOException
     */

    protected boolean checkToken() throws ServletException, IOException {
        String _token = getRequestParam(AttributeConst.TOKEN);

        if (_token == null || !(_token.equals(getTokenId()))) {
            //token未指定またはセッションIDと一致しない場合はエラー画面を表示
            forward(ForwardConst.FW_ERR_UNKNOWN);

            return false;
        } else {
            return true;
        }

    }

    /**
     * セッションIDを取得する
     * @return セッションID
     */
    protected String getTokenId() {
        return request.getSession().getId();
    }

    /**
     * リクエストから表示を要求されているページ数を取得し、返却する
     * @return 要求されているページ数(要求がない場合は1)
     */

    protected int getPage() {
        int page;
        page = toNumber(request.getParameter(AttributeConst.PAGE.getValue()));
        if (page == Integer.MIN_VALUE) {
            page = 1;
        }
        return page;
    }

    /**
     * 文字列を数値に変換
     * @param strNumber 変換前文字列
     * @return 変換後数値
     */

    protected int toNumber(String strNumber) {
        int number = 0;
        try {
            number = Integer.parseInt(strNumber);
        } catch (Exception e) {
            number = Integer.MIN_VALUE;
        }
        return number;
    }

    /**
     * 文字列をLocalDate型に変換する
     * @param strDate 変換前文字列
     * @return 変換後LocalDateインスタンス
     */

    protected LocalDate toLocalDate(String strDate) {
        if (strDate == null || strDate.equals("")) {
            return LocalDate.now();
        }
        return LocalDate.parse(strDate);
    }

    /**
     * リクエストパラメータから引数で指定したパラメータ名の値を返却する
     * @param key パラメータ名
     * @return パラメータの値
     */

    protected String getRequestParam(AttributeConst key) {
        return request.getParameter(key.getValue());
    }


    /**
     * リクエストスコープにパラメータを設定
     * @param key パラメータ名
     * @param value パラメータの値
     */

    protected <V> void putRequestScope(AttributeConst key, V value) {
        request.setAttribute(key.getValue(), value);
    }

    /**
     * セッションスコープから指定されたパラメータの値を取得し、返却する
     * @param key パラメータ名
     *@return パラメータの値
     */

    @SuppressWarnings("unchecked")
    protected <R> R getSessionScope(AttributeConst key) {
        return (R) request.getSession().getAttribute(key.getValue());
    }

    /**
     * セッションスコープにパラメータを設定する
     * @param key パラメータ名
     * @param value パラメータの値
     */

    protected <V> void putSessionScope(AttributeConst key, V Value) {
        request.getSession().setAttribute(key.getValue(), Value);
    }

    /**
     * セッションスコープから指定された名前のパラメータを除去する
     *@param key パラメータ名
     */

    protected void removeSessionScope(AttributeConst key) {
        request.getSession().removeAttribute(key.getValue());
    }


    /**
     * セッションスコープから指定されたパラメータの値を取得し、返却する
     * @param key パラメータ名
     * @return パラメータの値
     */

    @SuppressWarnings("unchecked")
    protected <R> R getContextScope(PropertyConst key) {
        return (R) context.getAttribute(key.getValue());
    }

}
