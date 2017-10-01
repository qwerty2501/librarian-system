package forms

import play.api.data.Forms._

object Validations {
  def statusTextValidation = nonEmptyText(1,500)
  def nameValidation = nonEmptyText(3,32)
  def passwordValication = nonEmptyText(8,16)
}
