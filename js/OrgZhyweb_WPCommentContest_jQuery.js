/*  Copyright 2009 - 2013 Comment Contest plug-in for Wordpress by Thomas "Zhykos" Cicognani  (email : tcicognani@zhyweb.org)

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

/*
 * Selected comment won't be used for contest
 * @param commentID Comment ID
 * @since 2.0
 */
function commentContestDelete(commentID) {
    jQuery("#comment-contest-" + commentID).removeClass("cheatComment");
    jQuery("#comment-contest-" + commentID).addClass("removedComment");
    jQuery("#restoreLink-" + commentID).show();
    jQuery("#deleteLink-" + commentID).hide();
    
    jQuery("#stopCheatLink-" + commentID).hide();
    jQuery("#cheatLink-" + commentID).show();
}

/*
 * Selected comment must be used for contest
 * @param commentID Comment ID
 * @since 2.0
 */
function commentContestRestore(commentID) {
    jQuery("#comment-contest-" + commentID).removeClass("removedComment");
    jQuery("#deleteLink-" + commentID).show();
    jQuery("#restoreLink-" + commentID).hide();
}

/**
 * Selected comment will win (if winners number greater than cheating comments number)
 * @param commentID Comment ID
 * @since 2.1
 */
function commentContestCheat(commentID) {
    jQuery("#comment-contest-" + commentID).removeClass("removedComment");
    jQuery("#comment-contest-" + commentID).addClass("cheatComment");
    jQuery("#stopCheatLink-" + commentID).show();
    jQuery("#cheatLink-" + commentID).hide();

    jQuery("#deleteLink-" + commentID).show();
    jQuery("#restoreLink-" + commentID).hide();
}

/**
 * Selected comment won't win anymore (except if it will be randomly choosen during the contest last step)
 * @param commentID Comment ID
 * @since 2.1
 */
function commentContestStopCheat(commentID) {
    jQuery("#comment-contest-" + commentID).removeClass("cheatComment");
    jQuery("#stopCheatLink-" + commentID).hide();
    jQuery("#cheatLink-" + commentID).show();
}

/*
 * Select all comments in the table which have the role "roleID"
 * @since 2.1
 */
function selectRole(roleID) {
    // Browse all table lines
    jQuery('#contestForm tr').each(function() {
        var line = jQuery(this);
        
        // Search for span tag with class zhyweb_comment_contest_role" which is equal to "roleID"
        // A user can have several roles, separated by a pipe (|)
        var rolesStr = line.find('.zhyweb_comment_contest_role').text();
        var roles = rolesStr.split('|');

        if (jQuery.inArray(roleID, roles) >= 0 && rolesStr.length > 0) {
            // Check the box in lines
            line.find('input[id^="cb-select"]').each(function() {
                jQuery(this).attr("checked", true);
            });
        }
    });
}

/*
 * Shuffle an array
 * @param myArray Array to shuffle
 * @see http://sedition.com/perl/javascript-fy.html
 * @since 2.0
 */
function fisherYates(myArray) {
    var i = myArray.length;
    if (i > 0) {
        while (--i) {
            var j = Math.floor( Math.random() * ( i + 1 ) );
            var tempi = myArray[i];
            var tempj = myArray[j];
            myArray[i] = tempj;
            myArray[j] = tempi;
        }
    }
}

// -----------------------------------------------------------------------------
// jQuery ready document

jQuery(document).ready(function() {
    
    // ------------------------ Tooltips (Help) --------------------------------

    // Code from BackWPup - WordPress Backup Plugin
    // http://marketpress.com/product/backwpup-pro/
    
    jQuery('.help').tipTip({
            'attribute': 'title',
            'fadeIn': 50,
            'fadeOut': 50,
            'keepAlive': true,
            'activation': 'hover',
            'maxWidth': 400
    });

    jQuery(".help").tipTip();
    
    // ------------------------ END Tooltips (Help) ----------------------------


    // ------------------------ DELETE COMMENTS FROM CONTEST -------------------
    /**
     * Remove all checked comments
     * @since 2.0
     */
    function deleteSelectedComments() {
        jQuery('#contestForm input[id^="cb-select"]:checked').each(function(index, domElem) {
            if (domElem.id.indexOf("-all-") == -1) {
                commentContestDelete(jQuery(this).val());
            } else {
                jQuery(this).attr("checked", false);
            }
        });
    }
    
    jQuery("#contestForm #doaction").click(function() {
        if (jQuery('#contestForm select[name="action"]').val() == 'delete') {
            deleteSelectedComments();
        }
    });
    
    jQuery("#contestForm #doaction2").click(function() {
        if (jQuery('#contestForm select[name="action2"]').val() == 'delete') {
            deleteSelectedComments();
        }
    });
    // ------------------------ end DELETE COMMENTS FROM CONTEST ---------------
    
    
    // ------------------------ RESTORE COMMENTS FOR CONTEST -------------------
    /**
     * Restore all checked comments
     * @since 2.0
     */
    function restoreSelectedComments() {
        jQuery('#contestForm input[id^="cb-select"]:checked').each(function(index, domElem) {
            if (domElem.id.indexOf("-all-") == -1) {
                commentContestRestore(jQuery(this).val());
            } else {
                jQuery(this).attr("checked", false);
            }
        });
    }
    
    jQuery("#contestForm #doaction").click(function() {
        if (jQuery('#contestForm select[name="action"]').val() == 'restore') {
            restoreSelectedComments();
        }
    });
    
    jQuery("#contestForm #doaction2").click(function() {
        if (jQuery('#contestForm select[name="action2"]').val() == 'restore') {
            restoreSelectedComments();
        }
    });
    // ------------------------ end RESTORE COMMENTS FOR CONTEST ---------------
    
    
    // ------------------------ RESULT TABLE -----------------------------------
    jQuery("#dialog-modal-winners").dialog({
        height: 500,
        width: 800,
        modal: true,
        autoOpen: false,
        dialogClass: 'wp-dialog',
        open: function() {
            // While opening the result dialog...
            var nbWinners = jQuery('#zwpcc_nb_winners').val();
            var commentsNormal = new Array();
            var commentsCheat = new Array();
            
            // Get all comments ID which are used for the contest
            jQuery('#contestForm tr').each(function() {
                var line = jQuery(this);
                
                // Get only normal et cheating lines
                if (!line.hasClass("removedComment")) {
                    var commentID = line.find('.zhyweb_comment_contest_id').html();

                    // Don't get table header and footer
                    if (commentID != null && commentID != "") {                        
                        if (line.hasClass("cheatComment")) {
                            commentsCheat.push(commentID);
                        } else {
                            commentsNormal.push(commentID);
                        }
                    }
                }
            });
            
            // Before displaying results, reset table
            jQuery('#dialog-modal-winners tr[id^="result-comment-contest"]').each(function() {
                jQuery(this).hide();
            });
            
            // Randomize arrays
            fisherYates(commentsCheat);
            if (commentsCheat.length < nbWinners) {
                // Optimisation
                fisherYates(commentsNormal);
            }
            
            // Show winners
            var comments = jQuery.merge(commentsCheat, commentsNormal);
            for (var i = 0; i < nbWinners && i < comments.length; i++) {
                jQuery("#result-comment-contest-" + comments[i]).show();
                if (i >= 1) {
                    // Sort table
                    jQuery("#result-comment-contest-" + comments[i - 1]).after(jQuery("#result-comment-contest-" + comments[i]));
                }
            }
        }
    });
    
    // Launch contest Button
    jQuery("#zwpcc_form").submit(function() {
        launch = true;
        
        // Clean all inputs
        jQuery(this).find('input').each(function() {
            jQuery(this).css('border', '1px solid rgb(223,223,223)');
        });
        
        // Check all values
        // "Number of winners" must be numeric
        if (!jQuery.isNumeric(jQuery("#zwpcc_nb_winners").val())) {
            launch = false;
            jQuery("#zwpcc_nb_winners").css('border', '2px solid red');
        }
        
        // Launch contest?
        if (launch) {
            jQuery("#dialog-modal-winners").dialog("open");
        }

        return false;
    });
    // ------------------------ end RESULT TABLE -------------------------------
    
});