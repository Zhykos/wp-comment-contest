/*  Copyright 2009 - 2014 Comment Contest plug-in for Wordpress by Thomas "Zhykos" Cicognani  (email : tcicognani@zhyweb.org)

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
        
        // Search for span tag with class "zhyweb_comment_contest_role" which is equal to "roleID"
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

/**
 * Open / Hide filters DIV component
 * @since 2.2
 */
function toggleFilters(pluginURL) {
    var filtersDIV = jQuery('#filters');
    
    filtersDIV.fadeToggle("slow", "linear", function() {
        if (filtersDIV.is(':visible')) {
            jQuery('#filtersImg').attr('src', pluginURL + '/img/minus.png');
        } else {
            jQuery('#filtersImg').attr('src', pluginURL + '/img/plus.png');
        }
    });
}

/**
 * Select duplicated lines in the table.
 * Lines are duplicated when a certain line parameter is the same in two lines.
 * @param cssClassParameter [string] CSS class name for the searched parameter
 * @since 2.2
 */
function selectDuplicates(cssClassParameter) {
    var line1nb = 0;
    jQuery('#contestForm tr').each(function() {
        // Browse all table lines
        var line1 = jQuery(this);
        
        var line2nb = 0;
        jQuery('#contestForm tr').each(function() {
            // Browse all table lines
            var line2 = jQuery(this);
            if (line1nb != line2nb) { // Compare two different lines
                var data1 = line1.find('.' + cssClassParameter).text();
                var data2 = line2.find('.' + cssClassParameter).text();
                if (data1 == data2 && data1 != "") {
                    // If data are equal => check both lines
                    line1.find('input[id^="cb-select"]').each(function() {
                        jQuery(this).attr("checked", true);
                    });
                    line2.find('input[id^="cb-select"]').each(function() {
                        jQuery(this).attr("checked", true);
                    });
                }
            }
            
            line2nb++;
        });
        
        line1nb++;
    });
}

// -----------------------------------------------------------------------------
// jQuery ready document

jQuery(document).ready(function() {
    
    // ------------------------ IP address filter ------------------------------
    
    jQuery('#ipAddressFilter').click(function() {
        selectDuplicates("zhyweb_comment_contest_ip");
    });
    
    // ------------------------ end IP address filter --------------------------
    
    // ------------------------ Email filter -----------------------------------
    
    jQuery('#emailAddressFilter').click(function() {
        selectDuplicates("zhyweb_comment_contest_email");
    });
    
    // ------------------------ end Email filter -------------------------------
    
    // ------------------------ Date filter ------------------------------------
    
    jQuery('#datepicker').datepicker();
    
    jQuery('#dateSubmit').click(function() {
        // Clear error messages
        jQuery('#zwpcc_dateFilter_error_message').hide();
        jQuery('#datepicker').css("border", "1px solid rgb(223,223,223)");
        jQuery('#dateHours').css("border", "1px solid rgb(223,223,223)");
        jQuery('#dateMinutes').css("border", "1px solid rgb(223,223,223)");
        
        // Check date format
        var dateFormatOk = false;
        var dateRegex = /^(\d{1,2})\/(\d{1,2})\/(\d{4})$/;
        var dateValue = jQuery('#datepicker').val();
        var month = "";
        var day = "";
        var year = "";
        if (dateRegex.test(dateValue)) {
            var match = dateRegex.exec(dateValue);
            month = match[1];
            day = match[2];
            year = match[3];
            if (month > 0 && month < 13 && day > 0 && day <32 && year > 0) {
                dateFormatOk = true;
            }
        }
        
        if (dateFormatOk) {
            // Check hours format
            var dateHours = jQuery('#dateHours').val();
            var dateMinutes = jQuery('#dateMinutes').val();
            if (dateHours != "" && dateHours >= 0 && dateHours < 24 && dateMinutes != "" && dateMinutes >= 0 && dateMinutes < 60) {
                // Date OK => Launch selection
                jQuery('#contestForm tr').each(function() {
                    // Browse all table lines
                    var line = jQuery(this);

                    // Search for span tag with class "zhyweb_comment_contest_timestamp"
                    var timestampComment = line.find('.zhyweb_comment_contest_timestamp').text();
                    if (timestampComment != "") {
                        var wantedTimestamp = year + month + day + dateHours + dateMinutes;
                        if (timestampComment > wantedTimestamp) {
                            // If comment time > deadline => Check the box in lines
                            line.find('input[id^="cb-select"]').each(function() {
                                jQuery(this).attr("checked", true);
                            });
                        }
                    }
                });
            } else {
                jQuery('#zwpcc_dateFilter_error_message').show();
                jQuery('#dateHours').css("border", "2px solid red");
                jQuery('#dateMinutes').css("border", "2px solid red");
            }
        } else {
            jQuery('#zwpcc_dateFilter_error_message').show();
            jQuery('#datepicker').css("border", "2px solid red");
        }
    });
    
    // ------------------------ END Date filter --------------------------------
    
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
                
                // Get only normal and cheating lines
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
                // Optimisation : randomize array only if necessary
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
        jQuery("#zwpcc_nbWinners_error_message").hide();
        jQuery(this).find('input').each(function() {
            jQuery(this).css('border', '1px solid rgb(223,223,223)');
        });
        
        // Check all values
        // "Number of winners" must be numeric
        var nbWinners = jQuery("#zwpcc_nb_winners").val();
        if (!jQuery.isNumeric(nbWinners) || nbWinners <= 0) {
            launch = false;
            jQuery("#zwpcc_nb_winners").css('border', '2px solid red');
            jQuery("#zwpcc_nbWinners_error_message").show();
        }
        
        // Launch contest?
        if (launch) {
            jQuery("#dialog-modal-winners").dialog("open");
        }

        return false;
    });
    // ------------------------ end RESULT TABLE -------------------------------
    
});