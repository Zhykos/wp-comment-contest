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
 * Display/Hide empty messages in table (messages shown because there isn't any comment in the table)
 */
function updateEmptyTableMessages() {
    // Display/Hide "no comment found" message in Contest table
    if (jQuery('#the-list-contest tr:visible').size() <= 0) {
        jQuery("#comment-contest-not-found-tr").show();
    } else {
        jQuery("#comment-contest-not-found-tr").hide();
    }
    
    // Display/Hide "no comment found" message in Removed Comments table
    if (jQuery('#the-list-no-contest tr:visible').size() <= 0) {
        jQuery("#comment-no-contest-not-found-tr").show();
    } else {
        jQuery("#comment-no-contest-not-found-tr").hide();
    }
}

/*
 * Move comment into Removed Comments (table with comment which wont be used for contest)
 */
function commentContestDelete(commentID) {
    jQuery("#comment-contest-" + commentID).hide();
    jQuery("#comment-no-contest-" + commentID).show();
    
    updateEmptyTableMessages();
}

/*
 * Move comment into Removed Comments (table with comment which wont be used for contest)
 */
function commentContestRestore(commentID) {
    jQuery("#comment-contest-" + commentID).show();
    jQuery("#comment-no-contest-" + commentID).hide();
    
    updateEmptyTableMessages();
}

/*
 * Select all comments in the table which have the role "roleID"
 */
function selectRoleInTable(roleID, tableID) {
    // Browse all table lines
    jQuery('#' + tableID + ' tr').each(function() {
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
 * Select all comments (in the contest table) which have the role "roleID"
 */
function selectRoleInContest(roleID) {
    selectRoleInTable(roleID, "inContestForm");
}

/*
 * Select all comments (in the deleted comments table) which have the role "roleID"
 */
function selectRoleInNoContest(roleID) {
    selectRoleInTable(roleID, "outContestForm");
}

/*
 * Shuffle an array
 * @param myArray Array to shuffle
 * @see http://sedition.com/perl/javascript-fy.html
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
    // ------------------------ DELETE COMMENTS FROM CONTEST -------------------------------
    function deleteSelectedComments() {
        jQuery('#inContestForm input[id^="cb-select"]:checked').each(function(index, domElem) {
            if (domElem.id.indexOf("-all-") == -1) {
                commentContestDelete(jQuery(this).val());
            } else {
                jQuery(this).attr("checked", false);
            }
        });
    }
    
    jQuery("#inContestForm #doaction").click(function() {
        if (jQuery('#inContestForm select[name="action"]').val() == 'delete') {
            deleteSelectedComments();
        }
    });
    
    jQuery("#inContestForm #doaction2").click(function() {
        if (jQuery('#inContestForm select[name="action2"]').val() == 'delete') {
            deleteSelectedComments();
        }
    });
    // ------------------------ end DELETE COMMENTS FROM CONTEST ---------------------------
    
    
    // ------------------------ RESTORE COMMENTS FOR CONTEST -------------------------------
    function restoreSelectedComments() {
        jQuery('#outContestForm input[id^="cb-select"]:checked').each(function(index, domElem) {
            if (domElem.id.indexOf("-all-") == -1) {
                commentContestRestore(jQuery(this).val());
            } else {
                jQuery(this).attr("checked", false);
            }
        });
    }
    
    jQuery("#outContestForm #doaction").click(function() {
        if (jQuery('#outContestForm select[name="action"]').val() == 'restore') {
            restoreSelectedComments();
        }
    });
    
    jQuery("#outContestForm #doaction2").click(function() {
        if (jQuery('#outContestForm select[name="action2"]').val() == 'restore') {
            restoreSelectedComments();
        }
    });
    // ------------------------ end RESTORE COMMENTS FOR CONTEST ---------------------------
    
    
    // ------------------------ CHECK FORM VALUES -------------------------------
    jQuery("#dialog-modal-winners").dialog({
        height: 500,
        width: 800,
        modal: true,
        autoOpen: false,
        dialogClass: 'wp-dialog',
        open: function() {
            // Get all comments ID which are used for the contest
            var comments = new Array();
            jQuery('#inContestForm tr').each(function() {
                var line = jQuery(this);
                if (line.css("display") != "none") {
                    // Get only displayed lines
                    var commentID = line.find('.zhyweb_comment_contest_id').html();
                    if (commentID != null && commentID != "") {
                        // Don't get table header and footer
                        comments.push(commentID);
                    }
                }
            });
            
            // Before displaying results, reset table
            jQuery('#dialog-modal-winners tr[id^="result-comment-contest"]').each(function() {
                jQuery(this).hide();
            });
            
            // Randomize array
            fisherYates(comments);
            
            // Display winners
            var nbWinners = jQuery('#zwpcc_nb_winners').val();
            for (var i = 0; i < nbWinners; i++) {
                jQuery("#result-comment-contest-" + comments[i]).show();
            }
        }
    });
    
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
    // ------------------------ end CHECK FORM VALUES -------------------------------
    
});