package forms

import play.api.data.Forms._

object Validations {
  def nameValidation = nonEmptyText(3,32)
  def passwordValication = nonEmptyText(8,16)
}
