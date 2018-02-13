<?php
/*  Copyright 2009 - 2018 Comment Contest plug-in for Wordpress by Thomas "Zhykos" Cicognani  (email : zhykos@gmail.com)

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

require_once 'TableComments.php';
require_once 'TableResults.php';

/**
 * Display the comment contest page
 * @author Thomas "Zhykos" Cicognani
 */
class WPCommentContest {
    /** Plug-in URL directory */
    private $pluginDir;
    
    /** Plug-in directory (on the server) */
    private $pluginSystemPath;
    
    public function __construct($pluginDir, $pluginSystemPath) {
        $this->pluginDir = $pluginDir;
        $this->pluginSystemPath = $pluginSystemPath;
    }
    
    /**
     * Display the contest page.
     * Current user has to be admin to access the page.
     * URL must have 'post' parameter as integer.
     */
    public function display() {
        if (isset($_GET['postID'])) {
            $postID = intval($_GET['postID']);
            if (current_user_can(10)) {
                if ($postID > 0) {
                    $this->displayComments($postID);
                } else {
                    $this->displayInfoPage(__("URL must have 'post' parameter as integer", "fr.zhykos.wordpress.commentcontest"));
                }
            } else {
                $this->displayInfoPage(__("You have to be administrator to access this page", "fr.zhykos.wordpress.commentcontest"));
            }
        } else {
            $this->displayInfoPage();
        }
    }
    
    /**
     * Display header page : title + form
     */
    private function displayHeaderPage($postID) {
        // Titles
        echo    "<div class=\"wrap\">
                    <form id=\"zwpcc_form\">
                        <h2>" . __("Comment Contest", "fr.zhykos.wordpress.commentcontest") . "</h2>";
        
        if ($postID != NULL) {
            echo "<h3>" . sprintf(__('Contest on post "%s"', "fr.zhykos.wordpress.commentcontest"), get_the_title($postID)) . "</h3>";
			echo "<div id='zwpcc_postID' style='display: none'>$postID</div>";

	        echo " <div id='winners-message-ok' class='updated' style='display: none'>" . __("Winners saved", "fr.zhykos.wordpress.commentcontest") . "</div>";
	        echo " <div id='winners-message-error' class='error' style='display: none'>" . __("Winners not saved!", "fr.zhykos.wordpress.commentcontest") . "<span id='winners-message-error-msg'></span></div>";

            // Contest parameters
            echo "<div id=\"zwpcc_nbWinners_error_message\" style=\"color: red; display: none;\">" . __('Number of winners error', "fr.zhykos.wordpress.commentcontest") . "</div>"
               . __('Number of winners:', "fr.zhykos.wordpress.commentcontest") . " <input type=\"text\" id=\"zwpcc_nb_winners\" value=\"1\"/>"
               . "<img src=\"$this->pluginDir/img/help.png\" alt=\"Help\" class=\"help\" title=\"". __('Number of comments used to determine winners', "fr.zhykos.wordpress.commentcontest") . "\" /><br /><br />"
               . "<input type=\"submit\" id=\"zwpcc_form_submit\" class=\"button action\" value=\"" . __('Launch contest', "fr.zhykos.wordpress.commentcontest") . "\" />";
            echo "</form>";

            // Result table : opened in a modal window
            echo "<div id=\"dialog-modal-winners\" title=\"" . __("Winners", "fr.zhykos.wordpress.commentcontest") . "\" style=\"display:none; margin: 10px\">";
	        echo "<input type=\"button\" class=\"button action saveWinnersButton\" value=\"" . __("Save winners", "fr.zhykos.wordpress.commentcontest") . "\" />";
	        $list = new TableResults($postID);
            $list->prepare_items();
            $list->display();
            echo "<input type=\"button\" class=\"button action saveWinnersButton\" value=\"" . __("Save winners", "fr.zhykos.wordpress.commentcontest") . "\" />";
            echo "</div>";
        }
    }
    
    /**
     * Display footer page
     */
    private function displayFooterPage() {
        echo "</div>"; // <div class="wrap">
    }
    
    /**
     * Display comments to choose for the contest
     * @param int $postID Post ID which contains comments
     */
    private function displayComments($postID) {
        global $wpdb;
        
        $query = "SELECT * FROM $wpdb->comments WHERE comment_approved = '1' AND comment_post_id='$postID' ORDER BY comment_date";
        $comments = $wpdb->get_results($query);

        if ($comments) {
            // Comments found
            $this->displayHeaderPage($postID);
            
            echo "<br /><br /><hr /><h3 style=\"float: left;\">" . __('Comments used for the contest:', "fr.zhykos.wordpress.commentcontest");
            
            // Help image
            echo " <img src=\"$this->pluginDir/img/help.png\" alt=\"Help\" class=\"help\" title=\"". __("The table below shows all available comments. You have to choose which ones you want for the contest.<br />"
                . " - You can select users who have the same Wordpress role (see User page).<br />"
                . " - You can remove comments from contest (don't be used to determine winners). Removed comments still are visible with red background color.<br />"
                . " - You can also cheat by adding comments into a cheating list. All cheating comments will always win! (only if the cheating list length is less than the winners number). Cheating comments still are visible with green background color.<br />"
                . " - The other comments (white/grey background) are only used if there isn't enough cheating comments.", "fr.zhykos.wordpress.commentcontest") . "\" />";
                
            echo "</h3>";
            // Filters
            $this->displayTemplate("filters");
            
            // Table
            echo "<div id='contestForm'>";
            $list = new TableComments($postID);
            $list->prepare_items();
            $list->views();
            $list->display();
            echo "</div>";
            
            // Footer
            $this->displayFooterPage();
        } else {
            // No comment because $postID is wrong
            $this->displayInfoPage(__("URL 'post' parameter has to be valid", "fr.zhykos.wordpress.commentcontest"));
        }
    }
    
    /**
     * Display information page in case of : wrong URL or menu page.
     * Only display HowTo.
     * @param string $debug Debug message or <code>NULL</code> if no message to display
     */
    private function displayInfoPage($debug = NULL) {
        $this->displayHeaderPage(NULL);
        $editPage = admin_url('edit.php');
        
        echo    "<h3>" . sprintf(__("To use the plug-in, go to the <a href=\"%s\">articles page</a> or <a href=\"%s\">pages page</a> then you'll find a link to launch contests", "fr.zhykos.wordpress.commentcontest"), $editPage, "$editPage?post_type=page") . "</h3>
                <br />"
                . __("A question, a bug, an idea, a contribution? Ask me anything on the support page:", "fr.zhykos.wordpress.commentcontest") . " <a href=\"https://wordpress.org/support/plugin/comment-contest\">https://wordpress.org/support/plugin/comment-contest</a><br />"
                . __("In case of a bug, for a quicker answer, please give my all the information about your website or generate a report file with the following link (no personal data is exported):", "fr.zhykos.wordpress.commentcontest") . " <a href=\"$this->pluginDir/php/GenerateDebugReport.php\">Report generator</a><br /><br />"
                . __("My email:") . " zhykos@gmail.com<br /><br />"
                . __("Official page:", "fr.zhykos.wordpress.commentcontest") . " <a href=\"http://wordpress.org/plugins/comment-contest/\">http://wordpress.org/plugins/comment-contest/</a><br />"
                . __("Github page:", "fr.zhykos.wordpress.commentcontest") . " <a href=\"https://github.com/Zhykos/wp-comment-contest/\">https://github.com/Zhykos/wp-comment-contest/</a>";
        
        if ($debug != NULL) {
            echo "<br /><br /><br />
                <i>" . __("Debug:", "fr.zhykos.wordpress.commentcontest") . " $debug</i>";
        }
            
        $this->displayFooterPage();
    }
    
    /**
     * Include and display a page (PHP speaking)
     * @param string $name Page name
     */
    private function displayTemplate($name) {
        $file = $this->pluginSystemPath . '/views/' . $name . '.php';
        require($file);
    }
}
